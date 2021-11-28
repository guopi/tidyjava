package pro.guopi.tidy


fun interface Flow<T> {
    @MustCallInMainPlane
    fun subscribe(subscriber: FlowSubscriber<T>)
}

interface FlowSubscriber<in T>: ErrorSubscriber {
    @MustCallInMainPlane
    fun onSubscribe(ss: Subscription)

    @MustCallInMainPlane
    fun onValue(value: T)

    @MustCallInMainPlane
    fun onComplete()
}

@FunctionalInterface
interface Subscription {
    @MustCallInMainPlane
    fun cancel()

    companion object {
        val TERMINATED = object : Subscription {
            override fun cancel() {
                //DO NOTHING
            }
        }

        @JvmStatic
        fun handleSubscriptionAlreadySet(current: Subscription, new: Subscription) {
            new.cancel()
            if (current !== TERMINATED) {
                Tidy.onError(IllegalStateException("Subscription already set!"))
            }
        }
    }
}

typealias FnOnSubscribe = (ss: Subscription) -> Unit
typealias FnOnValue<T> = (v: T) -> Unit
typealias FnOnComplete = () -> Unit
typealias FnOnError = (Throwable) -> Unit

