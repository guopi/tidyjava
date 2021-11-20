@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy.op

import pro.guopi.tidy.*

fun <T, R> YFuture<T>.map(mapper: (T) -> R): YFuture<R> {
    return FutureMap(this, mapper)
}

class FutureMap<T, R>(
    val source: YFuture<T>,
    val mapper: (T) -> R
) : YFuture<R>, YSubscriber<T>, YSubscription {
    private var upstream: YSubscription? = null
    private var downStream: YSubscriber<R>? = null

    override fun subscribe(ys: YSubscriber<R>) {
        this.downStream = ys
        source.subscribe(this)
    }

    override fun onSubscribe(ss: YSubscription) {
        upstream.let { up ->
            if (up === null) {
                upstream = ss
                downStream?.onSubscribe(this)
            } else {
                YErrors.handleSubscriptionAlreadySet(up, ss)
            }
        }
    }

    override fun onValue(v: T) {
        terminateFromUp()?.let { down ->
            try {
                val r = mapper(v)
                down.onValue(r)
            } catch (e: Throwable) {
                down.onError(e)
            }
        }
    }

    override fun onComplete() {
        terminateFromUp()?.onComplete()
    }

    override fun onError(e: Throwable) {
        terminateFromUp().handleError(e)
    }

    override fun cancel() {
        val up = upstream
        upstream = YSubscription.TERMINATED
        downStream = null
        up?.cancel()
    }

    private fun terminateFromUp(): YSubscriber<R>? {
        val down = downStream
        upstream = YSubscription.TERMINATED
        downStream = null
        return down
    }
}