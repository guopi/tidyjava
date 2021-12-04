@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Flowable
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.safeOnError
import java.util.*

fun <T, R> Flowable<T>.flatMap(
    mapper: (T) -> Flowable<R>,
    delayErrors: Boolean = false,
    maxConcurrency: Int = Int.MAX_VALUE,
): Flowable<R> {
    return FlowFlatMap(this, mapper, delayErrors, maxConcurrency)
}

class FlowFlatMap<T, R>(
    private val source: Flowable<T>,
    private val mapper: (T) -> Flowable<R>,
    private val delayErrors: Boolean,
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
        val delayErrors: Boolean,
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
            if (delayErrors) {
                delayError(error)
            } else {
                cancelAllChildren()
                val down = downStream
                downStream = null
                down.safeOnError(error)
            }
        }

        private fun delayError(error: Throwable) {
            val first = firstError
            if (first !== null) {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (first as java.lang.Throwable).addSuppressed(error)
            } else {
                firstError = error
            }
            tryFinishDownStream()
        }

        fun onChildError(child: ChildStream<R>, error: Throwable) {
            startingChildren.remove(child)

            if (upState !== FlowState.CANCELED) {
                if (delayErrors) {
                    delayError(error)
                } else {
                    cancelUpStream()
                    cancelAllChildren()
                    val down = downStream
                    downStream = null
                    down.safeOnError(error)
                }
            }
        }

        fun onChildValue(child: ChildStream<R>, value: R) {
            if (upState !== FlowState.CANCELED) {
                downStream?.onValue(value)
            }
        }

        fun onChildComplete(child: ChildStream<R>) {
            startingChildren.remove(child)
            if (upState !== FlowState.CANCELED) {
                tryFinishDownStream()
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
                subscribeChild(r)
            }
        }

        override fun onUpStreamComplete() {
            tryFinishDownStream()
        }

        private fun subscribeChild(childFlow: Flowable<R>) {
            if (canSubscribeChild()) {
                startChildFlow(childFlow)
            } else {
                createWaitingChildren().add(childFlow)
            }
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

        private fun tryFinishDownStream() {
            waitingChildren?.let {
                while (canSubscribeChild()) {
                    val first = it.pollFirst() ?: break
                    startChildFlow(first)
                }
            }
            if (startingChildren.isEmpty()) {
                downStream?.onComplete()
                downStream = null
            }
        }

        private fun cancelAllChildren() {
            startingChildren.forEach(ChildStream<R>::cancelChild)
            startingChildren.clear()
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