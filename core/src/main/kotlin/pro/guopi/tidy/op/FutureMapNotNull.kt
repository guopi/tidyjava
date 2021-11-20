@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.op

import pro.guopi.tidy.YFuture
import pro.guopi.tidy.YSubscriber

fun <T, R> YFuture<T>.mapNotNull(mapper: (T) -> R): YFuture<R> {
    return FutureMapNotNull(this, mapper)
}

class FutureMapNotNull<T, R>(
    val source: YFuture<T>,
    val mapper: (T) -> R?
) : YFuture<R> {
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