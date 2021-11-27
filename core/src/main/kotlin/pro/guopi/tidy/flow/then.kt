package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T> YFlow<T>.subscribe(
    onSubscribe: FnOnSubscribe? = null,
    onValue: FnOnValue<T>? = null,
    onComplete: FnOnComplete? = null,
    onError: FnOnError? = null
) {
    Tidy.runInMainPlane {
        this.subscribe(LambdaSubscriber(onSubscribe, onValue, onComplete, onError))
    }
}

class LambdaSubscriber<T>(
    private val onSubscribe: FnOnSubscribe? = null,
    private val onValue: FnOnValue<T>?,
    private val onComplete: FnOnComplete?,
    onError: FnOnError?
) : YSubscriber<T> {
    private val onError = onError ?: YErrors.ON_ERROR_DEFAULT
    private var upstream: YSubscription? = null

    override fun onSubscribe(ss: YSubscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = ss
                onSubscribe?.invoke(ss)
            } else {
                YErrors.handleSubscriptionAlreadySet(up, ss)
            }
        }
    }

    override fun onValue(v: T) {
        onValue?.invoke(v)
    }

    override fun onComplete() {
        onComplete?.invoke()
    }

    override fun onError(e: Throwable) {
        onError.invoke(e)
    }
}