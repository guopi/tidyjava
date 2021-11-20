package pro.guopi.tidy

fun <T> YFuture<T>.promise(): YPromise<T> {
    return YPromise(this)
}

class YPromise<T>(source: YFuture<T>) : YFuture<T>, YSubscriber<T> {
    private var upstream: YSubscription? = null
    private val downStreams = ArrayList<YSubscriber<T>>()   //todo mainPlane FastList
    private var result: Any? = null
    private var resultType = ResultType.NO

    private enum class ResultType { NO, VALUE, COMPLETE, ERROR }

    init {
        Y.start {
            source.subscribe(this)
        }
    }

    @MustCallInMainPlane
    override fun subscribe(ys: YSubscriber<T>) {
        when (resultType) {
            ResultType.VALUE -> {
                ys.onSubscribe(YSubscription.TERMINATED)
                @Suppress("UNCHECKED_CAST")
                ys.onValue(result as T)
            }
            ResultType.COMPLETE -> {
                ys.onSubscribe(YSubscription.TERMINATED)
                ys.onComplete()
            }
            ResultType.ERROR -> {
                ys.onSubscribe(YSubscription.TERMINATED)
                @Suppress("UNCHECKED_CAST")
                ys.onError(result as Throwable)
            }
            ResultType.NO -> {
                downStreams.add(ys)
                ys.onSubscribe(object : YSubscription {
                    override fun cancel() {
                        downStreams.remove(ys)
                    }
                })
            }
        }
    }

    override fun onSubscribe(ss: YSubscription) {
        this.upstream = ss
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