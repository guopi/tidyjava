package pro.guopi.tidy

import java.lang.annotation.Inherited


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Inherited
annotation class MustCallInMainPlane

@FunctionalInterface
fun interface YFuture<T> {
    @MustCallInMainPlane
    fun subscribe(ys: YSubscriber<T>)
}

@FunctionalInterface
fun interface YFlow<T> {
    @MustCallInMainPlane
    fun subscribe(ys: YSubscriber<T>)
}

interface YSubscriber<in T> {
    @MustCallInMainPlane
    fun onSubscribe(ss: YSubscription)

    @MustCallInMainPlane
    fun onValue(v: T)

    @MustCallInMainPlane
    fun onComplete()

    @MustCallInMainPlane
    fun onError(e: Throwable)
}

@FunctionalInterface
fun interface YSubscription {
    @MustCallInMainPlane
    fun cancel()

    object TERMINATED : YSubscription {
        override fun cancel() {
            //DO NOTHING
        }
    }
}

