package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T, R> Flow<T>.flatMap(mapper: (T) -> Flow<R>): Flow<R> {
    return FlowFlatMap(this, mapper)
}

class FlowFlatMap<T, R>(
    val source: Flow<T>,
    val mapper: (T) -> Flow<R>,
) : Flow<R> {
    override fun subscribe(subscriber: FSubscriber<R>) {
        source.subscribe(UpSubscriber(subscriber, this.mapper))
    }

    private class UpSubscriber<T, R>(
        downstream: FSubscriber<R>,
        private val mapper: (T) -> Flow<R>,
    ) : FilterSubscriber<T, R>(downstream) {

        override fun onValue(value: T) {
            downStream?.let { down ->
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

        override fun terminateWhenUpstreamFinish(): FSubscriber<R>? {
            childStream = FSubscription.TERMINATED
            return super.terminateWhenUpstreamFinish()
        }

        private inner class ChildSubscriber : FSubscriber<R> {
            override fun onSubscribe(ss: FSubscription) {
                childStream.let { child ->
                    if (child === null) {
                        childStream = ss
                    } else {
                        FSubscription.handleSubscriptionAlreadySet(child, ss)
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