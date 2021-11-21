package pro.guopi.tidy.op

import pro.guopi.tidy.*

fun <T, R> YFlow<T>.flatMap(mapper: (T) -> YFlow<R>): YFlow<R> {
    return FlowFlatMap(this, mapper)
}

class FlowFlatMap<T, R>(
    val source: YFlow<T>,
    val mapper: (T) -> YWish<R>
) : YFlow<R> {
    override fun subscribe(ys: YSubscriber<R>) {
        source.subscribe(UpSubscriber(ys, this.mapper))
    }

    private class UpSubscriber<T, R>(
        downstream: YSubscriber<R>,
        private val mapper: (T) -> YWish<R>
    ) : FilterSubscriber<T, R>(downstream) {

        override fun onValue(v: T) {
            downStream?.let { down ->
                try {
                    mapper(v).subscribe(ChildSubscriber())
                } catch (e: Throwable) {
                    terminateWhenErrorInHandle().handleError(e)
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

        override fun terminateWhenUpstreamFinish(): YSubscriber<R>? {
            childStream = YSubscription.TERMINATED
            return super.terminateWhenUpstreamFinish()
        }

        private inner class ChildSubscriber : YSubscriber<R> {
            override fun onSubscribe(ss: YSubscription) {
                childStream.let { child ->
                    if (child === null) {
                        childStream = ss
                    } else {
                        YErrors.handleSubscriptionAlreadySet(child, ss)
                    }
                }
            }

            override fun onValue(v: R) {
                terminateWhenUpstreamFinish()?.onValue(v)
            }

            override fun onComplete() {
                terminateWhenUpstreamFinish()?.onComplete()
            }

            override fun onError(e: Throwable) {
                terminateWhenUpstreamFinish()?.handleError(e)
            }
        }
    }

}