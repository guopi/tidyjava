package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.Tidy.Companion.main

fun <T> Promise.Companion.first(vararg promises: Promise<T>): Promise<T> {
    val ret = StdPromise<T>()
    val subscriber = TakeFistSubscriber(ret)

    main.start {
        promises.forEach { p ->
            p.subscribe(subscriber)
        }
    }
    return ret
}