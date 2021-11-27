package pro.guopi.tidy.flow

import pro.guopi.tidy.Flow
import pro.guopi.tidy.FSubscriber
import pro.guopi.tidy.safeOnError

fun <T, R> Flow<T>.mapNotNull(mapper: (T) -> R): Flow<R> {
    return FlowMapNotNull(this, mapper)
}

class FlowMapNotNull<T, R>(
    val source: Flow<T>,
    val mapper: (T) -> R?
) : Flow<R> {
    override fun subscribe(subscriber: FSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T, R>(subscriber) {
            override fun onValue(value: T) {
                downStream?.let { down ->
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