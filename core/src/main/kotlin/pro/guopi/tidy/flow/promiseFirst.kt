package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import pro.guopi.tidy.promise.StdPromise

fun <T> Flowable<T>.promiseFirst(): Promise<T> {
    val promise = StdPromise<T>()

    Tidy.main.start {
        subscribe(FlowFirstToPromise(promise))
    }
    return promise
}

private class FlowFirstToPromise<T>(promise: StdPromise<T>) : FlowSubscriber<T> {
    var promise: StdPromise<T>? = promise
    var upStream: Subscription? = null
    var state = FlowState.STARTED

    override fun onSubscribe(subscription: Subscription) {
        if (state === FlowState.STARTED) {
            state = FlowState.SUBSCRIBED
            upStream = subscription
        } else {
            Subscription.cannotOnSubscribe(upStream, subscription)
        }
    }

    inline fun doIfSubscribed(action: () -> Unit) {
        when (state) {
            FlowState.SUBSCRIBED -> {
                action()
            }
            FlowState.CANCELED -> return
            else -> Tidy.onError(IllegalStateException())
        }
    }

    override fun onValue(value: T) {
        doIfSubscribed {
            val up = upStream
            val p = promise
            state = FlowState.CANCELED
            upStream = null
            promise = null
            up?.cancel()
            p?.onSuccess(value)
        }
    }

    override fun onComplete() {
        doIfSubscribed {
            state = FlowState.TERMINATED
            upStream = null
            promise?.let { p ->
                promise = null
                if (p.isRunning()) {
                    p.onError(NoSuchElementException("The Flow is empty"))
                }
            }
        }
    }

    override fun onError(error: Throwable) {
        doIfSubscribed {
            state = FlowState.TERMINATED
            upStream = null
            promise?.let { p ->
                promise = null
                p.onError(error)
            }
        }
    }
}

