package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T, R> Flowable<T>.flatMap(mapper: (T) -> Flowable<R>): Flowable<R> {
    return FlowFlatMap(this, mapper)
}

class FlowFlatMap<T, R>(
    val source: Flowable<T>,
    val mapper: (T) -> Flowable<R>,
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(UpSubscriber(subscriber, this.mapper))
    }

    private class UpSubscriber<T, R>(
        downstream: FlowSubscriber<R>,
        private val mapper: (T) -> Flowable<R>,
    ) : FilterSubscriber<T, R>(downstream) {

        override fun onValue(value: T) {
            downstream?.let { down ->
                try {
                    mapper(value).subscribe(ChildSubscriber())
                } catch (e: Throwable) {
                    terminateWhenErrorInHandle().safeOnError(e)
                }
            }
        }

        override fun cancel() {
            super.cancel()
            childStream?.let {
                childStream = null
                it.cancel()
            }
        }

        override fun terminateWhenUpstreamFinish(): FlowSubscriber<R>? {
            childStream = Subscription.TERMINATED
            return super.terminateWhenUpstreamFinish()
        }

        private inner class ChildSubscriber : FlowSubscriber<R> {
            override fun onSubscribe(subscription: Subscription) {
                childStream.let { child ->
                    if (child === null) {
                        childStream = subscription
                    } else {
                        Subscription.handleSubscriptionAlreadySet(child, subscription)
                    }
                }
            }

            override fun onValue(value: R) {
                terminateWhenUpstreamFinish()?.onValue(value)
            }

            override fun onComplete() {
                terminateWhenUpstreamFinish()?.onComplete()
            }

            override fun onError(error: Throwable) {
                terminateWhenUpstreamFinish()?.safeOnError(error)
            }
        }
    }

}