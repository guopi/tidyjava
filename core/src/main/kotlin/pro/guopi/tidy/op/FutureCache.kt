package pro.guopi.tidy.op

import pro.guopi.tidy.YFuture
import pro.guopi.tidy.YSubscriber
import pro.guopi.tidy.YSubscription

fun <T> YFuture<T>.cache(): YFuture<T> {
    return FutureCache(this)
}

class FutureCache<T>(source: YFuture<T>) : YFuture<T>, YSubscriber<T> {
    private var upstream: YSubscription? = null
    private val downStreams = ArrayList<YSubscriber<T>>()   //todo mainPlane FastList
    private var result: Any? = null
    private var resultType = ResultType.NO

    private enum class ResultType { NO, VALUE, COMPLETE, ERROR }

    init {
        source.subscribe(this)
    }

    override fun subscribe(s: YSubscriber<T>) {
        when (resultType) {
            ResultType.VALUE -> {
                s.onSubscribe(YSubscription.TERMINATED)
                @Suppress("UNCHECKED_CAST")
                s.onValue(result as T)
            }
            ResultType.COMPLETE -> {
                s.onSubscribe(YSubscription.TERMINATED)
                s.onComplete()
            }
            ResultType.ERROR -> {
                s.onSubscribe(YSubscription.TERMINATED)
                @Suppress("UNCHECKED_CAST")
                s.onError(result as Throwable)
            }
            ResultType.NO -> {
                downStreams.add(s)
                //todo fast
                s.onSubscribe(object : YSubscription {
                    override fun cancel() {
                        downStreams.remove(s)
                    }
                })
            }
        }
    }

    override fun onSubscribe(subscription: YSubscription) {
        this.upstream = subscription
    }

    override fun onValue(v: T) {
        result = v
        resultType = ResultType.VALUE
    }

    override fun onComplete() {
        resultType = ResultType.COMPLETE
    }

    override fun onError(e: Throwable) {
        result = e
        resultType = ResultType.ERROR
    }
}