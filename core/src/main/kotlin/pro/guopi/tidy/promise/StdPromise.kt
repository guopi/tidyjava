package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.YErrors

class StdPromise<T> : Promise<T>, PromiseSubscriber<T> {
    private var result: Any? = null     // T | Throwable
    private var state = State.RUNNING

    //todo mainPlane FastList
    private val downStreams = ArrayList<PromiseSubscriber<T>>()

    enum class State { RUNNING, SUCCESS, ERROR }

    override fun subscribe(subscriber: PromiseSubscriber<T>) {
        when (state) {
            State.SUCCESS -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    subscriber.onSuccess(result as T)
                } catch (e: Throwable) {
                    YErrors.ON_ERROR_DEFAULT(e)
                }
            }
            State.ERROR -> {
                try {
                    @Suppress("UNCHECKED_CAST")
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

    override fun <R> fastMap(onSuccess: (T) -> R, onError: ((Throwable) -> R)?): Promise<R>? {
        return when (state) {
            State.SUCCESS -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    (Promise.success(onSuccess(result as T)))
                } catch (e: Throwable) {
                    return Promise.error(e)
                }
            }
            State.ERROR -> Promise.error(result as Throwable)
            else -> null
        }
    }

    override fun <R> fastThen(onSuccess: (T) -> Promise<R>, onError: ((Throwable) -> Promise<R>)?): Promise<R>? {
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
            else -> null
        }
    }

    override fun onSuccess(value: T) {
        onUpstreamEnd(State.SUCCESS, value)

        downStreams.forEach {
            it.onSuccess(value)
        }
        downStreams.clear()
    }

    override fun onError(error: Throwable) {
        onUpstreamEnd(State.ERROR, error)
        downStreams.forEach {
            it.onError(error)
        }
        downStreams.clear()
    }

    private fun onUpstreamEnd(state: State, result: Any?) {
        if (this.state === State.RUNNING) {
            this.result = result
            this.state = state
        } else {
            YErrors.handleError(IllegalStateException("Result already set!"))
        }
    }
}