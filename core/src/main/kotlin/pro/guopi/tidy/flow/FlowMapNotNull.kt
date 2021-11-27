package pro.guopi.tidy.flow

import pro.guopi.tidy.YFlow
import pro.guopi.tidy.YSubscriber
import pro.guopi.tidy.handleError

fun <T, R> YFlow<T>.mapNotNull(mapper: (T) -> R): YFlow<R> {
    return FlowMapNotNull(this, mapper)
}

class FlowMapNotNull<T, R>(
    val source: YFlow<T>,
    val mapper: (T) -> R?
) : YFlow<R> {
    override fun subscribe(ys: YSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T, R>(ys) {
            override fun onValue(v: T) {
                downStream?.let { down ->
                    try {
                        val r = mapper(v)
                        if (r !== null)
                            down.onValue(r)
                    } catch (e: Throwable) {
                        terminateWhenErrorInHandle().handleError(e)
                    }
                }
            }
        })
    }
}