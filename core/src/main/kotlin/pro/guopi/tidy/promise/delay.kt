package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Tidy
import java.util.concurrent.TimeUnit

fun <T> Promise<T>.delay(delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Promise<T> {
    val ret = StdPromise<T>()

    Tidy.runInMainPlane {
        this.subscribe(object : PromiseSubscriber<T> {
            override fun onSuccess(value: T) {
                Tidy.runDelay(delay, unit) {
                    ret.onSuccess(value)
                }
            }

            override fun onError(error: Throwable) {
                Tidy.runDelay(delay, unit) {
                    ret.onError(error)
                }
            }
        })
    }

    return ret
}