@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import java.util.*

fun <T, R> Flowable<T>.flatMap(
    mapper: (T) -> Flowable<R>,
    delayErrors: ErrorDelayMode = ErrorDelayMode.Immediate,
    maxConcurrency: Int = Int.MAX_VALUE,
): Flowable<R> {
    return FlowFlatMap(this, mapper, delayErrors, maxConcurrency)
}

enum class ErrorDelayMode {
    Immediate,
    UntilChildFlowError,
    UntilAllEnd
}

class FlowFlatMap<T, R>(
    private val source: Flowable<T>,
    private val mapper: (T) -> Flowable<R>,
    private val delayErrors: ErrorDelayMode,
    private val maxConcurrency: Int,
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(
            MainSubscriber(subscriber, this.mapper, delayErrors, maxConcurrency)
        )
    }

    private class MainSubscriber<T, R>(
        downStream: FlowSubscriber<R>,
        private val mapper: (T) -> Flowable<R>,
        val delayErrors: ErrorDelayMode,
        val maxConcurrency: Int,
    ) : WithUpStreamFlow<T>(), Subscription {
        private var downStream: FlowSubscriber<R>? = downStream
        private var firstError: Throwable? = null
        private val startingChildren = ArrayList<ChildStream<R>>() //todo fastlist
        private var waitingChildren: LinkedList<Flowable<R>>? = null //todo fastlist

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

        fun onChildError(child: ChildStream<R>, error: Throwable) {
            startingChildren.remove(child)

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

        fun onChildValue(child: ChildStream<R>, value: R) {
            if (upState !== FlowState.CANCELED) {
                downStream?.onValue(value)
            }
        }

        fun onChildComplete(child: ChildStream<R>) {
            startingChildren.remove(child)
            if (upState !== FlowState.CANCELED) {
                tryFinish()
            }
        }

        override fun cancel() {
            if (cancelUpStream()) {
                downStream = null
                cancelAllChildren()
            } else if (upState === FlowState.TERMINATED) {
                upState = FlowState.CANCELED
                cancelAllChildren()
            }
        }

        override fun onValue(value: T) {
            ifUpStreamSubscribed {
                val r = try {
                    mapper(value)
                } catch (e: Throwable) {
                    cancelUpStream()
                    onUpStreamError(e)
                    return
                }

                if (canSubscribeChild()) {
                    startChildFlow(r)
                } else {
                    createWaitingChildren().add(r)
                }
            }
        }

        override fun onUpStreamComplete() {
            tryFinish()
        }

        private fun startChildFlow(childFlow: Flowable<R>) {
            val childStream = ChildStream<R>(this)
            startingChildren.add(childStream)
            childFlow.subscribe(childStream)
        }

        private fun canSubscribeChild(): Boolean {
            return startingChildren.size < maxConcurrency
        }

        private inline fun createWaitingChildren() =
            (waitingChildren ?: LinkedList<Flowable<R>>().also { waitingChildren = it })

        private fun tryFinish() {
            waitingChildren?.let {
                while (canSubscribeChild()) {
                    val first = it.pollFirst() ?: break
                    startChildFlow(first)
                }
            }

            if (startingChildren.isEmpty() && upState === FlowState.TERMINATED) {
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
            startingChildren.forEach(ChildStream<R>::cancelChild)
            startingChildren.clear()
            waitingChildren?.clear()
        }
    }

    private class ChildStream<R>(val parent: MainSubscriber<*, R>) : WithUpStreamFlow<R>() {
        override fun onValue(value: R) {
            parent.onChildValue(this, value)
        }

        override fun onUpStreamComplete() {
            parent.onChildComplete(this)
        }

        override fun onUpStreamError(error: Throwable) {
            parent.onChildError(this, error)
        }

        override fun onUpStreamSubscribe() {
        }

        fun cancelChild() {
            cancelUpStream()
        }
    }
}