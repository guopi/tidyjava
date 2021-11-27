package pro.guopi.tidy.flow

import pro.guopi.tidy.YErrors
import pro.guopi.tidy.YSubscriber
import pro.guopi.tidy.YSubscription
import pro.guopi.tidy.handleError

abstract class FilterSubscriber<T, R>(
    downStream: YSubscriber<R>
) : YSubscriber<T>, YSubscription {
    protected var upstream: YSubscription? = null
    protected var downStream: YSubscriber<R>? = downStream

    override fun onSubscribe(ss: YSubscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = ss
                downStream?.onSubscribe(this)
            } else {
                YErrors.handleSubscriptionAlreadySet(up, ss)
            }
        }
    }

    override fun onComplete() {
        terminateWhenUpstreamFinish()?.onComplete()
    }

    override fun onError(e: Throwable) {
        terminateWhenUpstreamFinish().handleError(e)
    }

    override fun cancel() {
        upstream.let {
            upstream = YSubscription.TERMINATED
            downStream = null
            it?.cancel()
        }
    }

    protected open fun terminateWhenUpstreamFinish(): YSubscriber<R>? {
        val down = downStream
        upstream = YSubscription.TERMINATED
        downStream = null
        return down
    }

    protected open fun terminateWhenErrorInHandle(): YSubscriber<R>? {
        val up = upstream
        val down = downStream
        upstream = YSubscription.TERMINATED
        downStream = null

        up?.cancel()
        return down
    }
}