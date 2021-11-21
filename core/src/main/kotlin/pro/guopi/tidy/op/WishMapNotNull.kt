@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.op

import pro.guopi.tidy.YWish
import pro.guopi.tidy.YSubscriber

fun <T, R> YWish<T>.mapNotNull(mapper: (T) -> R): YWish<R> {
    return WishMapNotNull(this, mapper)
}

class WishMapNotNull<T, R>(
    val source: YWish<T>,
    val mapper: (T) -> R?
) : YWish<R> {
    override fun subscribe(ys: YSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T, R>(ys) {
            override fun onValue(v: T) {
                terminateWhenUpstreamFinish()?.let { down ->
                    try {
                        val r = mapper(v)
                        if (r !== null)
                            down.onValue(r)
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