package pro.guopi.tidy


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class MustCallInMainPlane

interface YFuture<T> {
    @MustCallInMainPlane
    fun subscribe(s: YSubscriber<T>)
}

interface YFlow<T> {
    @MustCallInMainPlane
    fun subscribe(s: YSubscriber<T>)
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

interface YSubscription {
    @MustCallInMainPlane
    fun cancel()

    object TERMINATED : YSubscription {
        override fun cancel() {
            //DO NOTHING
        }
    }
}

