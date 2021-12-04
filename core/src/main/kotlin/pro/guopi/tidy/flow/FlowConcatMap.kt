@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import java.util.*

fun <T, R> Flowable<T>.concatMap(
    mapper: (T) -> Flowable<R>,
    delayErrors: ErrorDelayMode = ErrorDelayMode.Immediate,
    bufferSize: Int = Int.MAX_VALUE,
): Flowable<R> {
    return FlowConcatMap(this, mapper, delayErrors, bufferSize)
}

class FlowConcatMap<T, R>(
    private val source: Flowable<T>,
    private val mapper: (T) -> Flowable<R>,
    private val delayErrors: ErrorDelayMode,
    private val bufferSize: Int,
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(
            MainSubscriber(subscriber, this.mapper, delayErrors, bufferSize)
        )
    }

    private class MainSubscriber<T, R>(
        downStream: FlowSubscriber<R>,
        private val mapper: (T) -> Flowable<R>,
        val delayErrors: ErrorDelayMode,
        val bufferSize: Int,
    ) : WithUpStreamFlow<T>(), Subscription {
        private var downStream: FlowSubscriber<R>? = downStream
        private var firstError: Throwable? = null
        private var currentChild: ChildStream<R>? = null
        private var waitingList: LinkedList<T>? = null //todo fastlist

        override fun onUpStreamSubscribe() {
            downStream?.onSubscribe(this)
        }

        override fun onUpStreamError(error: Throwable) {
            if (delayErrors === ErrorDelayMode.Immediate) {
                cancelAllChildren()
                val down = downStream
                downStream = null
                down.safeOnError(error)
            } else {
                delayError(error)
            }
        }

        fun onChildError(error: Throwable) {
            currentChild = null

            if (upState === FlowState.CANCELED) {
                Tidy.onError(error)
            } else {
                if (delayErrors === ErrorDelayMode.UntilAllEnd) {
                    delayError(error)
                } else {
                    cancelUpStream()
                    cancelAllChildren()
                    val down = downStream
                    downStream = null
                    down.safeOnError(firstError?.safeAddSuppressed(error) ?: error)
                }
            }
        }

        private fun delayError(error: Throwable) {
            val first = firstError
            if (first !== null) {
                first.safeAddSuppressed(error)
            } else {
                firstError = error
            }
            tryFinish()
        }

        fun onChildValue(value: R) {
            if (upState !== FlowState.CANCELED) {
                downStream?.onValue(value)
            }
        }

        fun onChildComplete() {
            currentChild = null
            if (upState !== FlowState.CANCELED) {
                tryFinish()
            }
        }

        override fun cancel() {
            cancelAllChildren()

            if (cancelUpStream()) {
                downStream = null
            } else if (upState === FlowState.TERMINATED) {
                upState = FlowState.CANCELED
            }
        }

        override fun onValue(value: T) {
            ifUpStreamSubscribed {
                if (currentChild === null) {
                    startChildFlow(value)
                } else {
                    addToWaitingList(value)
                }
            }
        }

        override fun onUpStreamComplete() {
            tryFinish()
        }

        private fun addToWaitingList(value: T) {
            val list = waitingList ?: LinkedList<T>().also {
                waitingList = it
            }
            if (list.size >= bufferSize) {
                Tidy.onError(TidyError("concatMap buffer reach limit:$bufferSize"))
                return
            }
            list.add(value)
        }

        private fun startChildFlow(value: T) {
            val r = try {
                mapper(value)
            } catch (e: Throwable) {
                cancelUpStream()
                onChildError(e)
                return
            }
            val childStream = ChildStream<R>(this)
            currentChild = childStream
            r.subscribe(childStream)
        }

        private fun tryFinish() {
            if (currentChild === null) {
                waitingList
                    ?.pollFirst()
                    ?.let(this::startChildFlow)
            }

            if (currentChild === null && upState === FlowState.TERMINATED) {
                downStream?.let { down ->
                    downStream = null

                    firstError.let { error ->
                        if (error !== null)
                            down.onError(error)
                        else
                            down.onComplete()
                    }
                }
            }
        }

        private fun cancelAllChildren() {
            currentChild?.let {
                currentChild = null
                it.cancelChild()
            }
            waitingList?.let {
                waitingList = null
                it.clear()
            }
        }
    }

    private class ChildStream<R>(val parent: MainSubscriber<*, R>) : WithUpStreamFlow<R>() {
        override fun onValue(value: R) {
            parent.onChildValue(value)
        }

        override fun onUpStreamComplete() {
            parent.onChildComplete()
        }

        override fun onUpStreamError(error: Throwable) {
            parent.onChildError(error)
        }

        override fun onUpStreamSubscribe() {
        }

        fun cancelChild() {
            cancelUpStream()
        }
    }
}