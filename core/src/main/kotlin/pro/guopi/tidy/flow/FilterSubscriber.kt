package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.safeOnError

abstract class FilterSubscriber<T, R>(
    downStream: FlowSubscriber<R>,
) : FlowSubscriber<T>, Subscription {
    protected var upStream: Subscription? = null
    protected var downStream: FlowSubscriber<R>? = downStream


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

    override fun onComplete() {
        terminateWhenUpstreamFinish()?.onComplete()
    }

    override fun onError(error: Throwable) {
        terminateWhenUpstreamFinish().safeOnError(error)
    }

    override fun cancel() {
        if (downStream !== null) {
            val up = upStream
            upStream = Subscription.TERMINATED
            downStream = null
            up?.cancel()
        }
    }

    protected open fun terminateWhenUpstreamFinish(): FlowSubscriber<R>? {
        val down = downStream
        upStream = Subscription.TERMINATED
        downStream = null
        return down
    }

    protected open fun terminateWhenErrorInHandle(): FlowSubscriber<R>? {
        val up = upStream
        val down = downStream
        upStream = Subscription.TERMINATED
        downStream = null

        up?.cancel()
        return down
    }
}