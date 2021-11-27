package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber
import pro.guopi.tidy.Y

fun <T> Promise.Companion.all(vararg promises: Promise<T>): Promise<Array<T>> {
    val ret = StdPromise<Array<T>>()
    Y.runInMainPlane {
        val values = arrayOfNulls<Any>(promises.size)
        var runningCount = promises.size    // -1 means error

        promises.forEachIndexed { index, p ->
            p.subscribe(object : PromiseSubscriber<T> {
                override fun onSuccess(value: T) {
                    if (runningCount <= 0) // error | all end
                        return
                    runningCount--
                    values[index] = value
                    if (runningCount == 0) {
                        @Suppress("UNCHECKED_CAST")
                        ret.onSuccess(values as Array<T>)
                    }
                }

                override fun onError(error: Throwable) {
                    runningCount = -1
                    ret.onError(error)
                }
            })
        }
    }
    return ret
}