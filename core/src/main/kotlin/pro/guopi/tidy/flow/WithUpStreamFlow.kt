package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Subscription
import pro.guopi.tidy.Tidy

abstract class WithUpStreamFlow<T> : FlowSubscriber<T> {
    protected var upStream: Subscription? = null
    protected var upState = FlowState.STARTED

    abstract fun onUpStreamComplete()
    abstract fun onUpStreamError(error: Throwable)
    abstract fun onUpStreamSubscribe()

    override fun onSubscribe(subscription: Subscription) {
        if (upState === FlowState.STARTED) {
            upState = FlowState.SUBSCRIBED
            upStream = subscription
            onUpStreamSubscribe()
        } else {
            Subscription.cannotOnSubscribe(upStream, subscription)
        }
    }

    protected inline fun ifUpStreamSubscribed(action: () -> Unit) {
        when (upState) {
            FlowState.SUBSCRIBED -> {
                action()
            }
            FlowState.CANCELED -> return
            else -> Tidy.onError(IllegalStateException())
        }
    }


    override fun onComplete() {
        ifUpStreamSubscribed {
            upState = FlowState.TERMINATED
            upStream = null
            onUpStreamComplete()
        }
    }

    override fun onError(error: Throwable) {
        ifUpStreamSubscribed {
            upState = FlowState.TERMINATED
            upStream = null
            onUpStreamError(error)
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