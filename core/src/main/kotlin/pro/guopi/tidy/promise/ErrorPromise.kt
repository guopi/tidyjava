package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber

class ErrorPromise<T>(private val error: Throwable) : Promise<T> {
    override fun subscribe(subscriber: PromiseSubscriber<T>) {
        subscriber.onError(error)
    }
}