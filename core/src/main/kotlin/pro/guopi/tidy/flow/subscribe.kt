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
) : WithFlowUpStream<T>(), Subscription {

    override fun cancel() {
        cancelUpStream()
    }

    override fun onSubscribeActual() {
        onSubscribe?.invoke(upStream!!)
    }

    override fun onValue(value: T) {
        doIfSubscribed {
            try {
                onValue?.invoke(value)
            } catch (e: Throwable) {
                cancelUpStream()
                Tidy.handleErrorUse(onError, e)
            }
        }
    }

    override fun onCompleteActual() {
        onComplete?.invoke()
    }

    override fun onErrorActual(error: Throwable) {
        Tidy.handleErrorUse(onError, error)
    }
}