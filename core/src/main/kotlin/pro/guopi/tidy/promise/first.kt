package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.Y

fun <T> Promise.Companion.first(vararg promises: Promise<T>): Promise<T> {
    val ret = StdPromise<T>()
    val subscriber = TakeFistSubscriber(ret)

    Y.runInMainPlane {
        promises.forEach { p ->
            p.subscribe(subscriber)
        }
    }
    return ret
}