package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Flowable
import pro.guopi.tidy.safeOnError

fun <T, R> Flowable<T>.map(mapper: (T) -> R): Flowable<R> {
    return FlowMap(this, mapper)
}

class FlowMap<T, R>(
    val source: Flowable<T>,
    val mapper: (T) -> R,
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T, R>(subscriber) {
            override fun onValue(value: T) {
                doIfSubscribed {
                    try {
                        val r = mapper(value)
                        downStream?.onValue(r)
                    } catch (e: Throwable) {
                        cancelUpStreamWhenOperator().safeOnError(e)
                    }
                }
            }
        })
    }
}