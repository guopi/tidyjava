@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Flowable
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.safeOnError

fun <T, R> Flowable<T>.flatMap(
    mapper: (T) -> Flowable<R>,
    delayErrors: Boolean = false,
    maxConcurrency: Int = Int.MAX_VALUE,
    bufferSize: Int = 256,
): Flowable<R> {
    return FlowFlatMap(this, mapper, delayErrors, maxConcurrency, bufferSize)
}

class FlowFlatMap<T, R>(
    val source: Flowable<T>,
    val mapper: (T) -> Flowable<R>,
    val delayErrors: Boolean,
    val maxConcurrency: Int,
    val bufferSize: Int,
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(
            UpSubscriber(subscriber, this.mapper, delayErrors, maxConcurrency, bufferSize)
        )
    }

    private class UpSubscriber<T, R>(
        downStream: FlowSubscriber<R>,
        private val mapper: (T) -> Flowable<R>,
        val delayErrors: Boolean,
        val maxConcurrency: Int,
        val bufferSize: Int,
    ) : WithFlowUpStream<T>(), Subscription {
        var downStream: FlowSubscriber<R>? = downStream
        var firstError: Throwable? = null

        override fun onSubscribeActual() {
            downStream?.onSubscribe(this)
        }

        override fun onErrorActual(error: Throwable) {
            if (delayErrors) {
                val first = firstError
                if (first !== null) {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (first as java.lang.Throwable).addSuppressed(error)
                } else {
                    firstError = error
                }
                tryFinishDownStream()
            } else {
                downStream.safeOnError(error)
                downStream = null
                cancelAllChildren()
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
            doIfSubscribed {
                val r = try {
                    mapper(value)
                } catch (e: Throwable) {
                    cancelUpStream()
                    onErrorActual(e)
                    return
                }
                subscribeChild(r)
            }
        }

        override fun onCompleteActual() {
            tryFinishDownStream()
        }

        private fun subscribeChild(r: Flowable<R>) {
            TODO("Not yet implemented")
        }

        private fun tryFinishDownStream() {
            TODO("Not yet implemented")
        }

        private fun cancelAllChildren() {
            TODO("Not yet implemented")
        }
    }

}