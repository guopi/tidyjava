package pro.guopi.tidy

interface YFuture<T> {
    fun subscribe(s: YSubscriber<T>)
}

interface YFlow<T> {
    fun subscribe(s: YSubscriber<T>)
}

interface YSubscriber<in T> {
    fun onSubscribe(s: YSubscription)
    fun onValue(v: T)
    fun onComplete()
    fun onError(e: Throwable)
}

interface YSubscription {
    fun cancel()

    object TERMINATED : YSubscription {
        override fun cancel() {
            //DO NOTHING
        }
    }
}

interface YPlane {
    fun submit(action: () -> Unit): YSubscription
}