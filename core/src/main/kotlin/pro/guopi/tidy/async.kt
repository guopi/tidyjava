package pro.guopi.tidy

import java.lang.annotation.Inherited
import java.util.concurrent.atomic.AtomicBoolean

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Inherited
annotation class MustCallInAsyncPlane

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Inherited
annotation class CallInAnyPlane

interface AsyncPlane {
    @CallInAnyPlane
    fun start(action: () -> Unit)

    @CallInAnyPlane
    fun submit(action: () -> Unit): AsyncSubscription
}

interface AsyncSubscriber<in T> {
    @MustCallInAsyncPlane
    fun onAsyncSubscribe(ss: AsyncSubscription)

    @MustCallInAsyncPlane
    fun onAsyncValue(v: T)

    @MustCallInAsyncPlane
    fun onAsyncComplete()

    @MustCallInAsyncPlane
    fun onAsyncError(e: Throwable)
}

interface AsyncSubscription {
    @MustCallInAsyncPlane
    fun cancel()
}


class AsyncSubscriberOnYSubscriber<T>(
    private val plane: AsyncPlane,
    private val s: YSubscriber<T>
) : AsyncSubscriber<T>, YSubscription, AtomicBoolean() {
    private var asyncSS: AsyncSubscription? = null

    @MustCallInMainPlane
    override fun cancel() {
        if (this.compareAndSet(false, true)) {
            asyncSS?.let { ss ->
                asyncSS = null
                plane.start {
                    ss.cancel()
                }
            }
        }
    }

    @MustCallInAsyncPlane
    override fun onAsyncSubscribe(ss: AsyncSubscription) {
        asyncSS = ss
        Y.start {
            s.onSubscribe(this)
        }
    }

    @MustCallInAsyncPlane
    override fun onAsyncValue(v: T) {
        if (get()) return

        Y.start { s.onValue(v) }
    }

    @MustCallInAsyncPlane
    override fun onAsyncComplete() {
        if (get()) return

        Y.start { s.onComplete() }
    }

    @MustCallInAsyncPlane
    override fun onAsyncError(e: Throwable) {
        if (get()) return

        Y.start { s.onError(e) }
    }
}
