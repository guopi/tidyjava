package pro.guopi.tidy

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class CallInAsyncPlane

interface AsyncPlane {
    fun start(action: () -> Unit)
    fun submit(action: () -> Unit): AsyncSubscription
}

interface AsyncSubscriber<in T> {
    @CallInAsyncPlane
    fun onAsyncSubscribe(ss: AsyncSubscription)

    @CallInAsyncPlane
    fun onAsyncValue(v: T)

    @CallInAsyncPlane
    fun onAsyncComplete()

    @CallInAsyncPlane
    fun onAsyncError(e: Throwable)
}

interface AsyncSubscription {
    @CallInAsyncPlane
    fun cancel()
}


class YSubscriptionOnAsyncSubscription(
    val plane: AsyncPlane,
    val s: AsyncSubscription
) : YSubscription {
    override fun cancel() {
        plane.start {
            s.cancel()
        }
    }
}

class AsyncSubscriberOnYSubscriber<T>(
    private val s: YSubscriber<T>,
    private val plane: AsyncPlane
) : AsyncSubscriber<T> {
    override fun onAsyncSubscribe(ss: AsyncSubscription) {
        Y.start {
            s.onSubscribe(YSubscriptionOnAsyncSubscription(plane, ss))
        }
    }

    override fun onAsyncValue(v: T) {
        Y.start { s.onValue(v) }
    }

    override fun onAsyncComplete() {
        Y.start { s.onComplete() }
    }

    override fun onAsyncError(e: Throwable) {
        Y.start { s.onError(e) }
    }
}
