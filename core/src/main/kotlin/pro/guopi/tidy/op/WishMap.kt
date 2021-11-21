@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.op

import pro.guopi.tidy.*

fun <T, R> YWish<T>.map(mapper: (T) -> R): YWish<R> {
    return WishMap(this, mapper)
}

class WishMap<T, R>(
    val source: YWish<T>,
    val mapper: (T) -> R
) : YWish<R> {
    override fun subscribe(ys: YSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T,R>(ys){
            override fun onValue(v: T) {
                terminateWhenUpstreamFinish()?.let { down ->
                    try {
                        val r = mapper(v)
                        down.onValue(r)
                    } catch (e: Throwable) {
                        down.onError(e)
                    }
                }
            }
        })
    }
}