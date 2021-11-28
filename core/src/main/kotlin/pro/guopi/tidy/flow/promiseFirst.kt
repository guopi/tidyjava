package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import pro.guopi.tidy.promise.StdPromise

fun <T> Flowable<T>.promiseFirst(): Promise<T> {
    val promise = StdPromise<T>()

    Tidy.main.start {
        subscribe(object : FlowSubscriber<T> {
            var upstream: Subscription? = null

            override fun onSubscribe(subscription: Subscription) {
                upstream.let { up ->
                    if (up === null) {
                        upstream = subscription
                    } else {
                        Subscription.handleSubscriptionAlreadySet(up, subscription)
                    }
                }
            }

            fun terminate(): Subscription? {
                val up = upstream
                upstream = Subscription.TERMINATED
                return up
            }


            override fun onValue(value: T) {
                terminate()?.cancel()
                promise.onSuccess(value)
            }

            override fun onComplete() {
                terminate()
                if (promise.isRunning()) {
                    promise.onError(NoSuchElementException("The Flow is empty"))
                }
            }

            override fun onError(error: Throwable) {
                terminate()
                promise.onError(error)
            }
        })
    }
    return promise
}

