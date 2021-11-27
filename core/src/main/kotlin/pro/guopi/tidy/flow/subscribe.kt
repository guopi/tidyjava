package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T> Flow<T>.subscribe(
    onValue: FnOnValue<T>? = null,
    onComplete: FnOnComplete? = null,
    onError: FnOnError? = null,
    onSubscribe: FnOnSubscribe? = null,
): FSubscription {
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
) : FSubscriber<T>, FSubscription {
    private var upstream: FSubscription? = null

    override fun cancel() {
        upstream?.cancel()
        upstream = FSubscription.TERMINATED
    }

    override fun onSubscribe(ss: FSubscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = ss
                onSubscribe?.invoke(ss)
            } else {
                FSubscription.handleSubscriptionAlreadySet(up, ss)
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