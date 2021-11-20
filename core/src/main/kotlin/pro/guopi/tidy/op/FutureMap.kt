@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.op

import pro.guopi.tidy.*

fun <T, R> YFuture<T>.map(mapper: (T) -> R): YFuture<R> {
    return FutureMap(this, mapper)
}

class FutureMap<T, R>(
    val source: YFuture<T>,
    val mapper: (T) -> R
) : YFuture<R> {
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