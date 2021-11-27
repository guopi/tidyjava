package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise

fun <T> Promise<T>.retry(retryTimes: Int, onError: (Throwable) -> Promise<T>): Promise<T> {
    if (retryTimes <= 0)
        return this

    return this.errorThen { error ->
        onError(error).retry(retryTimes - 1, onError)
    }
}