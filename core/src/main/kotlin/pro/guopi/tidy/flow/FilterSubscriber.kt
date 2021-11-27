package pro.guopi.tidy.flow

import pro.guopi.tidy.FSubscriber
import pro.guopi.tidy.FSubscription
import pro.guopi.tidy.safeOnError

abstract class FilterSubscriber<T, R>(
    downStream: FSubscriber<R>,
) : FSubscriber<T>, FSubscription {
    protected var upstream: FSubscription? = null
    protected var downStream: FSubscriber<R>? = downStream

    override fun onSubscribe(ss: FSubscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = ss
                downStream?.onSubscribe(this)
            } else {
                FSubscription.handleSubscriptionAlreadySet(up, ss)
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
            upstream = FSubscription.TERMINATED
            downStream = null
            it?.cancel()
        }
    }

    protected open fun terminateWhenUpstreamFinish(): FSubscriber<R>? {
        val down = downStream
        upstream = FSubscription.TERMINATED
        downStream = null
        return down
    }

    protected open fun terminateWhenErrorInHandle(): FSubscriber<R>? {
        val up = upstream
        val down = downStream
        upstream = FSubscription.TERMINATED
        downStream = null

        up?.cancel()
        return down
    }
}