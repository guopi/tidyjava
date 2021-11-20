package pro.guopi.tidy.op

import pro.guopi.tidy.YFuture
import pro.guopi.tidy.YSubscriber

fun <T> YFuture<T>.filter(filter: (T) -> Boolean): YFuture<T> {
    return FutureFilter(this, filter)
}

class FutureFilter<T>(
    val source: YFuture<T>,
    val filter: (T) -> Boolean
) : YFuture<T> {
    override fun subscribe(ys: YSubscriber<T>) {
        source.subscribe(object : FilterSubscriber<T, T>(ys) {
            override fun onValue(v: T) {
                terminateWhenUpstreamFinish()?.let { down ->
                    try {
                        if (filter(v))
                            down.onValue(v)
                        else
                            down.onComplete()
                    } catch (e: Throwable) {
                        down.onError(e)
                    }
                }
            }
        })
    }
}