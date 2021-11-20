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
    fun start(action: Runnable)

    @CallInAnyPlane
    fun submit(action: Runnable): AsyncSubscription
}

interface AsyncSubscriber<in T> {
    @MustCallInAsyncPlane
    fun isCanceled(): Boolean

    @MustCallInAsyncPlane
    fun onAsyncValue(v: T)

    @MustCallInAsyncPlane
    fun onAsyncComplete()

    @MustCallInAsyncPlane
    fun onAsyncError(e: Throwable)
}

@FunctionalInterface
fun interface AsyncSubscription {
    @MustCallInAsyncPlane
    fun cancel()
}

class AsyncTask<T>(
    private val plane: AsyncPlane,
    private val ys: YSubscriber<T>,
    private val action: (s: AsyncSubscriber<T>) -> Unit
) : AsyncSubscriber<T>, YSubscription, AtomicBoolean(), Runnable {
    private var submitted: AsyncSubscription? = null

    @MustCallInMainPlane
    override fun cancel() {
        if (this.compareAndSet(false, true)) {
            submitted?.let { sss ->
                submitted = null
                plane.start {
                    sss.cancel()
                }
            }
        }
    }

    override fun isCanceled(): Boolean {
        return get()
    }

    override fun run() {
        if (get()) return
        action(this)
    }

    @CallInAnyPlane
    fun submit() {
        submitted = plane.submit(this)
        Y.start {
            ys.onSubscribe(this)
        }
    }

    @MustCallInAsyncPlane
    override fun onAsyncValue(v: T) {
        if (get()) return

        Y.start { ys.onValue(v) }
    }

    @MustCallInAsyncPlane
    override fun onAsyncComplete() {
        if (get()) return

        Y.start { ys.onComplete() }
    }

    @MustCallInAsyncPlane
    override fun onAsyncError(e: Throwable) {
        if (get()) return

        Y.start { ys.onError(e) }
    }
}
