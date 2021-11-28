package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T> Flowable<T>.subscribe(
    onValue: FnOnValue<T>? = null,
    onComplete: FnOnComplete? = null,
    onError: FnOnError? = null,
    onSubscribe: FnOnSubscribe? = null,
): Subscription {
    val lambdaSubscriber = LambdaSubscriber(onSubscribe, onValue, onComplete, onError)
    Tidy.main.start {
        this.subscribe(lambdaSubscriber)
    }
    return lambdaSubscriber
}

class LambdaSubscriber<T>(
    private val onSubscribe: FnOnSubscribe?,
    private val onValue: FnOnValue<T>?,
    private val onComplete: FnOnComplete?,
    private val onError: FnOnError?,
) : FlowSubscriber<T>, Subscription {
    private var upstream: Subscription? = null

    override fun cancel() {
        upstream?.cancel()
        upstream = Subscription.TERMINATED
    }

    override fun onSubscribe(subscription: Subscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = subscription
                onSubscribe?.invoke(subscription)
            } else {
                Subscription.handleSubscriptionAlreadySet(up, subscription)
            }
        }
    }

    override fun onValue(value: T) {
        onValue?.invoke(value)
    }

    override fun onComplete() {
        onComplete?.invoke()
    }

    override fun onError(error: Throwable) {
        Tidy.handleErrorUse(onError, error)
    }
}