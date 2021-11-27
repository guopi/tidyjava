package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.Tidy
import java.util.concurrent.TimeUnit

fun Promise.Companion.timer(delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Promise<Unit> {
    val ret = StdPromise<Unit>()
    Tidy.main.startDelay(delay, unit) {
        ret.onSuccess(Unit)
    }
    return ret
}