package pro.guopi.tidy.promise

import pro.guopi.tidy.*

fun <T, R> Promise<T>.map(onSuccess: (T) -> R, onError: ((Throwable) -> R)? = null): Promise<R> {
    val fast = fastMap(onSuccess, onError)
    if (fast !== null)
        return fast

    val ret = NormalPromise<R>()
    Y.runInMainPlane {
        this.subscribe(object : PromiseSubscriber<T> {
            override fun onSuccess(v: T) {
                val r = try {
                    onSuccess(v)
                } catch (e: Throwable) {
                    YErrors.handleError(e)
                    return
                }
                ret.onSuccess(r)
            }

            override fun onError(error: Throwable) {
                if (onError !== null) {
                    val r = try {
                        onError(error)
                    } catch (e: Throwable) {
                        ret.onError(e)
                        return
                    }
                    ret.onSuccess(r)
                } else {
                    ret.onError(error)
                }
            }
        })
    }
    return ret
}

fun <T> Promise<T>.mapError(onError: (Throwable) -> T): Promise<T> {
    return map(::identity, onError)
}

fun <T> Promise<T>.finally(onResult: (error: Throwable?, value: T?) -> Unit): Promise<T> {
    return map(
        { v ->
            onResult(null, v)
            v
        },
        { error ->
            onResult(error, null)
            throw error
        }
    )
}