package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.Tidy
import pro.guopi.tidy.safeOnError

abstract class FilterSubscriber<T, R>(
    downStream: FlowSubscriber<R>,
) : WithFlowUpStream<T>(), Subscription {
    protected var downStream: FlowSubscriber<R>? = downStream

    override fun onSubscribeActual() {
        downStream?.onSubscribe(this)
    }

    protected fun clearDownStream(): FlowSubscriber<R>? {
        val down = downStream
        downStream = null
        return down
    }

    override fun onCompleteActual() {
        clearDownStream()?.onComplete()
    }

    override fun onErrorActual(error: Throwable) {
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