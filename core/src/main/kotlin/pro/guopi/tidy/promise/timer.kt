package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.Tidy
import java.util.concurrent.TimeUnit

fun Promise.Companion.timeout(delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Promise<Unit> {
    val ret = StdPromise<Unit>()
    Tidy.runDelay(delay, unit) {
        ret.onSuccess(Unit)
    }
    return ret
}