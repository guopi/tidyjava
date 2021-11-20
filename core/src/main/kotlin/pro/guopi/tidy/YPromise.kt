package pro.guopi.tidy

fun <T> YFuture<T>.promise(): YPromise<T> {
    return YPromise(this)
}

class YPromise<T>(source: YFuture<T>) : YFuture<T>, YSubscriber<T> {
    private var upstream: YSubscription? = null
    private var result: Any? = null     // V | Throwable
    private var resultType = ResultType.NO

    //todo mainPlane FastList
    private val downStreams = ArrayList<YSubscriber<T>>()

    private enum class ResultType { NO, VALUE, COMPLETE, ERROR }

    init {
        Y.runInMainPlane {
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
                ys.onSubscribe(YSubscription {
                    downStreams.remove(ys)
                })
            }
        }
    }

    override fun onSubscribe(ss: YSubscription) {
        this.upstream = ss
    }

    override fun onValue(v: T) {
        terminateFromUp(ResultType.VALUE, v)

        downStreams.forEach {
            it.onValue(v)
        }
        downStreams.clear()
    }

    override fun onComplete() {
        terminateFromUp(ResultType.COMPLETE, null)

        downStreams.forEach(YSubscriber<T>::onComplete)
        downStreams.clear()
    }

    override fun onError(e: Throwable) {
        terminateFromUp(ResultType.ERROR, e)
        downStreams.forEach {
            it.onError(e)
        }
        downStreams.clear()
    }

    private fun terminateFromUp(resultType: ResultType, result: Any?) {
        if (this.resultType === ResultType.NO) {
            this.result = result
            this.resultType = resultType
            upstream = YSubscription.TERMINATED
        } else {
            YErrors.defaultOnError(IllegalStateException("Result already set!"))
        }
    }
}