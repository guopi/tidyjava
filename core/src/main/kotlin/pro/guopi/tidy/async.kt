package pro.guopi.tidy

interface AsyncSubscriber<in T> {
    fun onAsyncSubscribe(subscription: YSubscription)
    fun onAsyncValue(v: T)
    fun onAsyncComplete()
    fun onAsyncError(e: Throwable)
}
