package pro.guopi.tidy.flow

import pro.guopi.tidy.Flowable

fun <T> Flowable<T>.takeFirst(): Flowable<T> {
    return Flowable { downstream ->
        this.subscribe(object : FilterSubscriber<T, T>(downstream) {
            override fun onValue(value: T) {
                upstream?.cancel()
                terminateWhenUpstreamFinish()?.onValue(value)
            }
        })
    }
}