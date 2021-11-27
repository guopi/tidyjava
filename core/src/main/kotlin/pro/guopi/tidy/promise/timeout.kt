package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.Y
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> Promise<T>.timeout(delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Promise<T> {
    val ret = NormalPromise<T>()
    val subscriber = TerminatableSubsciber(ret)

    Y.runDelay(delay, unit) {
        if (subscriber.terminate()) {
            ret.onError(TimeoutException())
        }
    }

    this.subscribe(subscriber)

    return ret
}
