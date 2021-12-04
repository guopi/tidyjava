package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.Tidy
import pro.guopi.tidy.safeOnError

abstract class FilterSubscriber<T, R>(
    downStream: FlowSubscriber<R>,
) : WithUpStreamFlow<T>(), Subscription {
    protected var downStream: FlowSubscriber<R>? = downStream

    override fun onUpStreamSubscribe() {
        downStream?.onSubscribe(this)
    }

    protected fun clearDownStream(): FlowSubscriber<R>? {
        val down = downStream
        downStream = null
        return down
    }

    override fun onUpStreamComplete() {
        clearDownStream()?.onComplete()
    }

    override fun onUpStreamError(error: Throwable) {
        clearDownStream().safeOnError(error)
    }

    override fun cancel() {
        if (cancelUpStream()) {
            downStream = null
        }
    }

    protected open fun cancelUpStreamWhenOperator(): FlowSubscriber<R>? {
        if (!cancelUpStream()) {
            Tidy.onError(IllegalStateException())
        }
        return downStream.also {
            downStream = null
        }
    }
}