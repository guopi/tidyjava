package pro.guopi.tidy


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class CallInMainPlane

interface YFuture<T> {
    @CallInMainPlane
    fun subscribe(s: YSubscriber<T>)
}

interface YFlow<T> {
    @CallInMainPlane
    fun subscribe(s: YSubscriber<T>)
}

interface YSubscriber<in T> {
    @CallInMainPlane
    fun onSubscribe(ss: YSubscription)

    @CallInMainPlane
    fun onValue(v: T)

    @CallInMainPlane
    fun onComplete()

    @CallInMainPlane
    fun onError(e: Throwable)
}

interface YSubscription {
    @CallInMainPlane
    fun cancel()

    object TERMINATED : YSubscription {
        override fun cancel() {
            //DO NOTHING
        }
    }
}

