package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Tidy

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
                    Tidy.onError(e)
                }
            }
            State.ERROR -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    subscriber.onError(result as Throwable)
                } catch (e: Throwable) {
                    Tidy.onError(e)
                }
            }
            State.RUNNING -> {
                downStreams.add(subscriber)
            }
        }
    }

    override fun onSuccess(value: T) {
        if (onUpstreamEnd(State.SUCCESS, value)) {
            downStreams.forEach {
                it.onSuccess(value)
            }
            downStreams.clear()
        }
    }

    override fun onError(error: Throwable) {
        if (onUpstreamEnd(State.ERROR, error)) {
            downStreams.forEach {
                it.onError(error)
            }
            downStreams.clear()
        }
    }

    private fun onUpstreamEnd(state: State, result: Any?): Boolean {
        if (this.state === State.RUNNING) {
            this.result = result
            this.state = state
            return true
        } else {
            Tidy.onError(IllegalStateException("Result already set!"))
            return false
        }
    }

    fun isRunning(): Boolean = state === State.RUNNING
}