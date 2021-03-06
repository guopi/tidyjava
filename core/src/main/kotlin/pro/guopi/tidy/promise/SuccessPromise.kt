package pro.guopi.tidy.promise

import pro.guopi.tidy.Promise
import pro.guopi.tidy.PromiseSubscriber

class SuccessPromise<T>(private val value: T) : Promise<T> {
    override fun subscribe(subscriber: PromiseSubscriber<T>) {
        subscriber.onSuccess(value)
    }
}