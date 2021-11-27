package pro.guopi.tidy

import java.lang.annotation.Inherited


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Inherited
annotation class MustCallInMainPlane

fun interface Flow<T> {
    @MustCallInMainPlane
    fun subscribe(subscriber: FSubscriber<T>)
}

interface FSubscriber<in T> {
    @MustCallInMainPlane
    fun onSubscribe(ss: FSubscription)

    @MustCallInMainPlane
    fun onValue(value: T)

    @MustCallInMainPlane
    fun onComplete()

    @MustCallInMainPlane
    fun onError(error: Throwable)
}

@FunctionalInterface
interface FSubscription {
    @MustCallInMainPlane
    fun cancel()


    companion object {
        val TERMINATED = object : FSubscription {
            override fun cancel() {
                //DO NOTHING
            }
        }

        @JvmStatic
        fun handleSubscriptionAlreadySet(current: FSubscription, new: FSubscription) {
            new.cancel()
            if (current !== TERMINATED) {
                Tidy.onError(IllegalStateException("Subscription already set!"))
            }
        }
    }
}

typealias FnOnSubscribe = (ss: FSubscription) -> Unit
typealias FnOnValue<T> = (v: T) -> Unit
typealias FnOnComplete = () -> Unit
typealias FnOnError = (Throwable) -> Unit

