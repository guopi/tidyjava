package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.Tidy

fun <T> Promise.Companion.first(vararg promises: Promise<T>): Promise<T> {
    val ret = StdPromise<T>()
    val subscriber = TakeFistSubscriber(ret)

    Tidy.runInMainPlane {
        promises.forEach { p ->
            p.subscribe(subscriber)
        }
    }
    return ret
}