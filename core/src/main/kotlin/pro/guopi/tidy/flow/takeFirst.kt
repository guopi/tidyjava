package pro.guopi.tidy.flow

import pro.guopi.tidy.Flowable

fun <T> Flowable<T>.takeFirst(): Flowable<T> {
    return Flowable { downstream ->
        this.subscribe(object : FilterSubscriber<T, T>(downstream) {
            override fun onValue(value: T) {
                ifUpStreamSubscribed {
                    cancelUpStreamWhenOperator()?.onValue(value)
                }
            }
        })
    }
}