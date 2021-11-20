package pro.guopi.tidy.op

import pro.guopi.tidy.*

fun <T, R> YFlow<T>.map(mapper: (T) -> R): YFlow<R> {
    return FlowMap(this, mapper)
}

class FlowMap<T, R>(
    val source: YFlow<T>,
    val mapper: (T) -> R
) : YFlow<R>, YSubscriber<T>, YSubscription {
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
        downStream?.let { down ->
            try {
                val r = mapper(v)
                down.onValue(r)
            } catch (e: Throwable) {
                terminateWhenHandle().handleError(e)
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
        upstream.let {
            upstream = YSubscription.TERMINATED
            downStream = null
            it?.cancel()
        }
    }

    private fun terminateFromUp(): YSubscriber<R>? {
        val down = downStream
        upstream = YSubscription.TERMINATED
        downStream = null
        return down
    }

    private fun terminateWhenHandle(): YSubscriber<R>? {
        val up = upstream
        val down = downStream
        upstream = YSubscription.TERMINATED
        downStream = null

        up?.cancel()
        return down
    }
}