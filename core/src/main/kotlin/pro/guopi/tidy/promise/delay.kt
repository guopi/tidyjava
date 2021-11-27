package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Y
import java.util.concurrent.TimeUnit

fun <T> Promise<T>.delay(delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Promise<T> {
    val ret = NormalPromise<T>()

    Y.runInMainPlane {
        this.subscribe(object : PromiseSubscriber<T> {
            override fun onSuccess(value: T) {
                Y.runDelay(delay, unit) {
                    ret.onSuccess(value)
                }
            }

            override fun onError(error: Throwable) {
                Y.runDelay(delay, unit) {
                    ret.onError(error)
                }
            }
        })
    }

    return ret
}