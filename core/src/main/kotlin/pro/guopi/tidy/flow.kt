package pro.guopi.tidy


fun interface Flowable<T> {
    @MustCallInMainPlane
    fun subscribe(subscriber: FlowSubscriber<T>)
}

interface FlowSubscriber<in T> : ErrorSubscriber {
    @MustCallInMainPlane
    fun onSubscribe(subscription: Subscription)

    @MustCallInMainPlane
    fun onValue(value: T)

    @MustCallInMainPlane
    fun onComplete()
}

typealias FnOnSubscribe = (ss: Subscription) -> Unit
typealias FnOnValue<T> = (v: T) -> Unit
typealias FnOnComplete = () -> Unit
typealias FnOnError = (Throwable) -> Unit

