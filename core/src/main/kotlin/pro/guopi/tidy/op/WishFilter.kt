package pro.guopi.tidy.op

import pro.guopi.tidy.YWish
import pro.guopi.tidy.YSubscriber

fun <T> YWish<T>.filter(filter: (T) -> Boolean): YWish<T> {
    return WishFilter(this, filter)
}

class WishFilter<T>(
    val source: YWish<T>,
    val filter: (T) -> Boolean
) : YWish<T> {
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