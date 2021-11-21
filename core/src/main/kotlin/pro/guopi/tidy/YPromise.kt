package pro.guopi.tidy

fun <T> YWish<T>.promise(): YPromise<T> {
    return PromiseOnWish(this)
}


interface YPromise<T> {
    fun <R> map(onSuccess: (T) -> R, onError: ((Throwable) -> R)? = null): YPromise<R>
    fun <R> flatMap(onSuccess: (T) -> YPromise<R>, onError: ((Throwable) -> YPromise<R>)? = null): YPromise<R>
    fun <R> mapError(onError: ((Throwable) -> R)? = null): YPromise<R>
    fun <R> flatMapError(onError: ((Throwable) -> YPromise<R>)? = null): YPromise<R>

    companion object {
        @JvmStatic
        fun <T> create(action: FnPromiseCreateAction<T>): YPromise<T> {

        }
    }
}

typealias FnPromiseOnSuccess<T> = (T) -> Unit
typealias FnPromiseOnError = (Throwable) -> Unit
typealias FnPromiseCreateAction<T> = (onSuccess: FnPromiseOnSuccess<T>, onError: FnPromiseOnError) -> Unit

class PromiseOnWish<T>(wish: YWish<T>) : YPromise<T>, YSubscriber<T> {
    private var upstream: YSubscription? = null
    private var result: Any? = null     // V | Throwable
    private var resultType = ResultType.NO

    //todo mainPlane FastList
    private val downStreams = ArrayList<YSubscriber<T>>()

    private enum class ResultType { NO, VALUE, COMPLETE, ERROR }

    init {
        Y.runInMainPlane {
            wish.subscribe(this)
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