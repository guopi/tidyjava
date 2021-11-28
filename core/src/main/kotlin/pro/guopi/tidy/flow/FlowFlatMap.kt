@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T, R> Flowable<T>.flatMap(
    mapper: (T) -> Flowable<R>,
    delayErrors: Boolean = false,
    maxConcurrency: Int = Int.MAX_VALUE,
    bufferSize: Int = 256,
): Flowable<R> {
    return FlowFlatMap(this, mapper)
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
    ) : FlowSubscriber<T>, Subscription {
        var upStream: Subscription? = null
        var downStream: FlowSubscriber<R>? = downStream
        var firstError: Throwable? = null

        override fun onSubscribe(subscription: Subscription) {
            upStream.let { up ->
                if (up === null) {
                    upStream = subscription
                    downStream?.onSubscribe(this)
                } else {
                    Subscription.handleSubscriptionAlreadySet(up, subscription)
                }
            }
        }

        private inline fun isTerminatedOrCanceled(): Boolean {
            return Subscription.isTerminatedOrCanceled(upStream)
        }

        override fun onError(error: Throwable) {
            if (isTerminatedOrCanceled()) {
                Tidy.onError(error)
                return
            }

            if (delayErrors) {
                val first = firstError
                if (first !== null) {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (first as java.lang.Throwable).addSuppressed(error)
                } else {
                    firstError = error
                }
                onUpStreamEnd()
            } else {
                upStream = Subscription.TERMINATED
                downStream.safeOnError(error)
                downStream = null
                cancelAllChildren()
            }
        }

        override fun cancel() {
            val up = upStream
            if (up !== Subscription.CANCELED) {
                upStream = Subscription.CANCELED
                downStream = null
                up?.cancel()
                cancelAllChildren()
            }
        }

        override fun onComplete() {
            if (upStream !== Subscription.CANCELED) {
                upStream = Subscription.TERMINATED
                onUpStreamEnd()
            }
        }

        private fun onUpStreamEnd() {
            tryEndDownStream()
        }

        private fun tryEndDownStream() {
            TODO("Not yet implemented")
        }

        private fun cancelAllChildren() {
            TODO("Not yet implemented")
        }
    }

}