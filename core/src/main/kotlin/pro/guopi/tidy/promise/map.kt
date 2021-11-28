package pro.guopi.tidy.promise

import pro.guopi.tidy.*
import pro.guopi.tidy.Tidy.Companion.main

fun <T, R> Promise<T>.map(onSuccess: (T) -> R, onError: ((Throwable) -> R)? = null): Promise<R> {
    val ret = StdPromise<R>()
    main.start {
        this.subscribe(object : PromiseSubscriber<T> {
            override fun onSuccess(value: T) {
                val r = try {
                    onSuccess(value)
                } catch (e: Throwable) {
                    Tidy.onError(e)
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
