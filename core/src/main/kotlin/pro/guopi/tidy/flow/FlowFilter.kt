package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Flowable
import pro.guopi.tidy.safeOnError

fun <T> Flowable<T>.filter(filter: (T) -> Boolean): Flowable<T> {
    return FlowFilter(this, filter)
}

class FlowFilter<T>(
    val source: Flowable<T>,
    val filter: (T) -> Boolean,
) : Flowable<T> {
    override fun subscribe(subscriber: FlowSubscriber<T>) {
        source.subscribe(object : FilterSubscriber<T, T>(subscriber) {
            override fun onValue(value: T) {
                doIfSubscribed {
                    try {
                        if (filter(value))
                            downStream?.onValue(value)
                    } catch (e: Throwable) {
                        cancelUpStreamWhenOperator().safeOnError(e)
                    }
                }
            }
        })
    }
}