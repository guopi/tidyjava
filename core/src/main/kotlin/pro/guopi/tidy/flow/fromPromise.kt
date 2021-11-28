package pro.guopi.tidy.flow

import pro.guopi.tidy.*

fun <T> Promise<T>.toFlow(): Flowable<T> {
    return Flowable {
        val subscriber = PromiseSubscriberAsFlow(it)
        it.onSubscribe(subscriber)
        this.subscribe(subscriber)
    }
}

private class PromiseSubscriberAsFlow<T>(
    downstream: FlowSubscriber<T>,
) : PromiseSubscriber<T>, Subscription {
    private var downstream: FlowSubscriber<T>? = downstream

    override fun onSuccess(value: T) {
        terminate()?.let {
            it.onValue(value)
            it.onComplete()
        }
    }

    override fun onError(error: Throwable) {
        terminate().safeOnError(error)
    }


    private fun terminate(): FlowSubscriber<T>? {
        return if (downstream !== null) {
            val down = downstream
            downstream = null
            down
        } else {
            null
        }
    }

    override fun cancel() {
        terminate()
    }
}