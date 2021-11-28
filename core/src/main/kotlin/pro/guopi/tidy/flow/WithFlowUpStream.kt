package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.Tidy

abstract class WithFlowUpStream<T> : FlowSubscriber<T> {
    protected var upStream: Subscription? = null
    protected var upState = FlowState.STARTED

    abstract fun onCompleteActual()
    abstract fun onErrorActual(error: Throwable)
    abstract fun onSubscribeActual()

    override fun onSubscribe(subscription: Subscription) {
        if (upState === FlowState.STARTED) {
            upState = FlowState.SUBSCRIBED
            upStream = subscription
            onSubscribeActual()
        } else {
            Subscription.cannotOnSubscribe(upStream, subscription)
        }
    }

    protected inline fun doIfSubscribed(action: () -> Unit) {
        when (upState) {
            FlowState.SUBSCRIBED -> {
                action()
            }
            FlowState.CANCELED -> return
            else -> Tidy.onError(IllegalStateException())
        }
    }


    override fun onComplete() {
        doIfSubscribed {
            upState = FlowState.TERMINATED
            upStream = null
            onCompleteActual()
        }
    }

    override fun onError(error: Throwable) {
        doIfSubscribed {
            upState = FlowState.TERMINATED
            upStream = null
            onErrorActual(error)
        }
    }

    protected fun cancelUpStream(): Boolean {
        return if (upState == FlowState.STARTED || upState == FlowState.SUBSCRIBED) {
            upState = FlowState.CANCELED
            upStream?.let {
                upStream = null
                it.cancel()
            }
            true
        } else {
            false
        }
    }

}