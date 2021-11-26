package pro.guopi.tidy


interface PromiseSubscriber<T> {
    @MustCallInMainPlane
    fun onSuccess(v: T)

    @MustCallInMainPlane
    fun onError(error: Throwable)
}

interface Promise<T> {
    @MustCallInMainPlane
    fun subscribeInMainPlane(subscriber: PromiseSubscriber<T>)

    fun <R> map(onSuccess: (T) -> R, onError: ((Throwable) -> R)? = null): Promise<R> {
        val ret = NormalPromise<R>()
        Y.runInMainPlane {
            this.subscribeInMainPlane(object : PromiseSubscriber<T> {
                override fun onSuccess(v: T) {
                    val r = try {
                        onSuccess(v)
                    } catch (e: Throwable) {
                        ret.onError(e)
                        return
                    }
                    ret.onSuccess(r)
                }

                override fun onError(error: Throwable) {
                    ret.onError(error)
                }
            })
        }
        return ret
    }

    fun <R> then(onSuccess: (T) -> Promise<R>, onError: ((Throwable) -> Promise<R>)? = null): Promise<R> {
        val downStream = NormalPromise<R>()
        Y.runInMainPlane {
            this.subscribeInMainPlane(object : PromiseSubscriber<T> {
                override fun onSuccess(v: T) {
                    try {
                        onSuccess(v).subscribeInMainPlane(downStream)
                    } catch (e: Throwable) {
                        downStream.onError(e)
                    }
                }

                override fun onError(error: Throwable) {
                    try {
                        if (onError !== null) {
                            onError(error).subscribeInMainPlane(downStream)
                        } else {
                            downStream.onError(error)
                        }
                    } catch (e: Throwable) {
                        downStream.onError(e)
                    }
                }
            })
        }
        return downStream
    }

    companion object {
        @JvmStatic
        fun <T> create(action: (PromiseSubscriber<T>) -> Unit): Promise<T> {
            val promise = NormalPromise<T>()
            Y.runInMainPlane {
                action(promise)
            }
            return promise
        }

        @JvmStatic
        fun <T> success(v: T): Promise<T> {
            TODO()
        }

        @JvmStatic
        fun <T> error(e: Throwable): Promise<T> {
            TODO()
        }
    }

}


// fun <R> mapError(onError: ((Throwable) -> R)? = null): YPromise<R>
// fun <R> flatMapError(onError: ((Throwable) -> YPromise<R>)? = null): YPromise<R>


class NormalPromise<T> : Promise<T>, PromiseSubscriber<T> {
    private var result: Any? = null     // T | Throwable
    private var state = State.RUNNING

    //todo mainPlane FastList
    private val downStreams = ArrayList<PromiseSubscriber<T>>()

    private enum class State { RUNNING, SUCCESS, ERROR }

    @Suppress("UNCHECKED_CAST")
    override fun subscribeInMainPlane(subscriber: PromiseSubscriber<T>) {
        when (state) {
            State.SUCCESS -> {
                try {
                    subscriber.onSuccess(result as T)
                } catch (e: Throwable) {
                    YErrors.ON_ERROR_DEFAULT(e)
                }
            }
            State.ERROR -> {
                try {
                    subscriber.onError(result as Throwable)
                } catch (e: Throwable) {
                    YErrors.ON_ERROR_DEFAULT(e)
                }
            }
            State.RUNNING -> {
                downStreams.add(subscriber)
            }
        }
    }

    override fun <R> map(onSuccess: (T) -> R, onError: ((Throwable) -> R)?): Promise<R> {
        when (state) {
            State.SUCCESS -> {
                val mapped = try {
                    @Suppress("UNCHECKED_CAST")
                    onSuccess(result as T)
                } catch (e: Throwable) {
                    return Promise.error(e)
                }
                return Promise.success(mapped)
            }
            State.ERROR -> return Promise.error(result as Throwable)
            State.RUNNING -> return super.map(onSuccess, onError)
        }
    }

    override fun <R> then(onSuccess: (T) -> Promise<R>, onError: ((Throwable) -> Promise<R>)?): Promise<R> {
        return when (state) {
            State.SUCCESS -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    onSuccess(result as T)
                } catch (e: Throwable) {
                    Promise.error(e)
                }
            }
            State.ERROR -> Promise.error(result as Throwable)
            State.RUNNING -> super.then(onSuccess, onError)
        }
    }

    override fun onSuccess(v: T) {
        onEnd(State.SUCCESS, v)

        downStreams.forEach {
            it.onSuccess(v)
        }
        downStreams.clear()
    }

    override fun onError(error: Throwable) {
        onEnd(State.ERROR, error)
        downStreams.forEach {
            it.onError(error)
        }
        downStreams.clear()
    }

    private fun onEnd(state: State, result: Any?) {
        if (this.state === State.RUNNING) {
            this.result = result
            this.state = state
        } else {
            YErrors.defaultOnError(IllegalStateException("Result already set!"))
        }
    }
}