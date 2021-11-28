package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.safeOnError

abstract class FilterSubscriber<T, R>(
    downStream: FlowSubscriber<R>,
) : FlowSubscriber<T>, Subscription {
    protected var upstream: Subscription? = null
    protected var downStream: FlowSubscriber<R>? = downStream

    override fun onSubscribe(ss: Subscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = ss
                downStream?.onSubscribe(this)
            } else {
                Subscription.handleSubscriptionAlreadySet(up, ss)
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
            downStream = null
            it?.cancel()
        }
    }

    protected open fun terminateWhenUpstreamFinish(): FlowSubscriber<R>? {
        val down = downStream
        upstream = Subscription.TERMINATED
        downStream = null
        return down
    }

    protected open fun terminateWhenErrorInHandle(): FlowSubscriber<R>? {
        val up = upstream
        val down = downStream
        upstream = Subscription.TERMINATED
        downStream = null

        up?.cancel()
        return down
    }
}