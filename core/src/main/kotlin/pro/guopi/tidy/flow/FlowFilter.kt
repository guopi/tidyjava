package pro.guopi.tidy.flow

import pro.guopi.tidy.Flow
import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.safeOnError

fun <T> Flow<T>.filter(filter: (T) -> Boolean): Flow<T> {
    return FlowFilter(this, filter)
}

class FlowFilter<T>(
    val source: Flow<T>,
    val filter: (T) -> Boolean
) : Flow<T> {
    override fun subscribe(subscriber: FlowSubscriber<T>) {
        source.subscribe(object : FilterSubscriber<T, T>(subscriber) {
            override fun onValue(value: T) {
                downStream?.let { down ->
                    try {
                        if (filter(value))
                            down.onValue(value)
                    } catch (e: Throwable) {
                        terminateWhenErrorInHandle().safeOnError(e)
                    }
                }
            }
        })
    }
}