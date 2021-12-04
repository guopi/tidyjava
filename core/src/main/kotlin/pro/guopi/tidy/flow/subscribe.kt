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
) : WithUpStreamFlow<T>(), Subscription {

    override fun cancel() {
        cancelUpStream()
    }

    override fun onUpStreamSubscribe() {
        onSubscribe?.invoke(upStream!!)
    }

    override fun onValue(value: T) {
        ifUpStreamSubscribed {
            try {
                onValue?.invoke(value)
            } catch (e: Throwable) {
                cancelUpStream()
                Tidy.handleErrorUse(onError, e)
            }
        }
    }

    override fun onUpStreamComplete() {
        onComplete?.invoke()
    }

    override fun onUpStreamError(error: Throwable) {
        Tidy.handleErrorUse(onError, error)
    }
}