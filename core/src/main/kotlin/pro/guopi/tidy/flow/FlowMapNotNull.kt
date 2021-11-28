package pro.guopi.tidy.flow

import pro.guopi.tidy.Flowable
import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.safeOnError

fun <T, R> Flowable<T>.mapNotNull(mapper: (T) -> R): Flowable<R> {
    return FlowMapNotNull(this, mapper)
}

class FlowMapNotNull<T, R>(
    val source: Flowable<T>,
    val mapper: (T) -> R?
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T, R>(subscriber) {
            override fun onValue(value: T) {
                downstream?.let { down ->
                    try {
                        val r = mapper(value)
                        if (r !== null)
                            down.onValue(r)
                    } catch (e: Throwable) {
                        terminateWhenErrorInHandle().safeOnError(e)
                    }
                }
            }
        })
    }
}