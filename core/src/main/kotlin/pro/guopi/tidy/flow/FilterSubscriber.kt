package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.safeOnError

abstract class FilterSubscriber<T, R>(
    downStream: FlowSubscriber<R>,
) : FlowSubscriber<T>, Subscription {
    protected var upstream: Subscription? = null
    protected var downstream: FlowSubscriber<R>? = downStream

    override fun onSubscribe(subscription: Subscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = subscription
                downstream?.onSubscribe(this)
            } else {
                Subscription.handleSubscriptionAlreadySet(up, subscription)
            }
        }
    }

    override fun onComplete() {
        terminateWhenUpstreamFinish()?.onComplete()
    }

    override fun onError(error: Throwable) {
        terminateWhenUpstreamFinish().safeOnError(error)
    }

    override fun cancel() {
        upstream.let {
            upstream = Subscription.TERMINATED
            downstream = null
            it?.cancel()
        }
    }

    protected open fun terminateWhenUpstreamFinish(): FlowSubscriber<R>? {
        val down = downstream
        upstream = Subscription.TERMINATED
        downstream = null
        return down
    }

    protected open fun terminateWhenErrorInHandle(): FlowSubscriber<R>? {
        val up = upstream
        val down = downstream
        upstream = Subscription.TERMINATED
        downstream = null

        up?.cancel()
        return down
    }
}