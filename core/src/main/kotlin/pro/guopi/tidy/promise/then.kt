package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Y

fun <T, R> Promise<T>.then(
    onSuccess: (T) -> Promise<R>,
    onError: ((Throwable) -> Promise<R>)? = null,
): Promise<R> {
    val fast = fastThen(onSuccess, onError)
    if (fast !== null)
        return fast

    val downStream = NormalPromise<R>()
    Y.runInMainPlane {
        this.subscribe(object : PromiseSubscriber<T> {
            override fun onSuccess(v: T) {
                val r = try {
                    onSuccess(v)
                } catch (e: Throwable) {
                    downStream.onError(e)
                    return
                }
                r.subscribe(downStream)
            }

            override fun onError(error: Throwable) {
                try {
                    if (onError !== null) {
                        val r = try {
                            onError(error)
                        } catch (e: Throwable) {
                            downStream.onError(e)
                            return
                        }
                        r.subscribe(downStream)
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

fun <T> Promise<T>.errorThen(onError: (Throwable) -> Promise<T>): Promise<T> {
    return then(Promise.Companion::success, onError)
}
