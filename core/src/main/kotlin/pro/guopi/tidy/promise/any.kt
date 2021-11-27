package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Y

fun <T> Promise.Companion.first(vararg promises: Promise<T>): Promise<T> {
    val ret = NormalPromise<T>()

    Y.runInMainPlane {
        var ended = false

        promises.forEach { p ->
            p.subscribe(object : PromiseSubscriber<T> {
                override fun onSuccess(value: T) {
                    if (!ended) {
                        ended = true
                        ret.onSuccess(value)
                    }
                }

                override fun onError(error: Throwable) {
                    if (!ended) {
                        ended = true
                        ret.onError(error)
                    }
                }
            })
        }
    }
    return ret
}