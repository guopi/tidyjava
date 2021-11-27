package pro.guopi.tidy.flow

import pro.guopi.tidy.YFlow
import pro.guopi.tidy.YSubscriber
import pro.guopi.tidy.handleError

fun <T> YFlow<T>.filter(filter: (T) -> Boolean): YFlow<T> {
    return FlowFilter(this, filter)
}

class FlowFilter<T>(
    val source: YFlow<T>,
    val filter: (T) -> Boolean
) : YFlow<T> {
    override fun subscribe(ys: YSubscriber<T>) {
        source.subscribe(object : FilterSubscriber<T, T>(ys) {
            override fun onValue(v: T) {
                downStream?.let { down ->
                    try {
                        if (filter(v))
                            down.onValue(v)
                    } catch (e: Throwable) {
                        terminateWhenErrorInHandle().handleError(e)
                    }
                }
            }
        })
    }
}