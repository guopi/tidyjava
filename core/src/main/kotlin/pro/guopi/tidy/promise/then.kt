package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Tidy.Companion.main

fun <T, R> Promise<T>.then(
    onSuccess: (T) -> Promise<R>,
    onError: ((Throwable) -> Promise<R>)? = null,
): Promise<R> {
    val fast = fastThen(onSuccess, onError)
    if (fast !== null)
        return fast

    val ret = StdPromise<R>()
    main.start {
        this.subscribe(object : PromiseSubscriber<T> {
            override fun onSuccess(value: T) {
                val r = try {
                    onSuccess(value)
                } catch (e: Throwable) {
                    ret.onError(e)
                    return
                }
                r.subscribe(ret)
            }

            override fun onError(error: Throwable) {
                try {
                    if (onError !== null) {
                        val r = try {
                            onError(error)
                        } catch (e: Throwable) {
                            ret.onError(e)
                            return
                        }
                        r.subscribe(ret)
                    } else {
                        ret.onError(error)
                    }
                } catch (e: Throwable) {
                    ret.onError(e)
                }
            }
        })
    }
    return ret
}

fun <T> Promise<T>.errorThen(onError: (Throwable) -> Promise<T>): Promise<T> {
    return then(Promise.Companion::success, onError)
}
