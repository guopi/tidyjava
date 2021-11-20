package pro.guopi.tidy.op

import pro.guopi.tidy.*

fun <T> YFuture<T>.then(
    onValue: FnOnValue<T>? = null,
    onComplete: FnOnComplete? = null,
    onError: FnOnError? = null
) {
    Y.start {
        this.subscribe(FutureThenSubscriber(onValue, onComplete, onError))
    }
}

class FutureThenSubscriber<T>(
    val onValue: FnOnValue<T>?,
    val onComplete: FnOnComplete?,
    val onError: FnOnError?
) : YSubscriber<T> {
    override fun onSubscribe(ss: YSubscription) {
        //DO NOTHING
    }

    override fun onValue(v: T) {
        onValue?.invoke(v)
    }

    override fun onComplete() {
        onComplete?.invoke()
    }

    override fun onError(e: Throwable) {
        onError?.invoke(e)
    }
}