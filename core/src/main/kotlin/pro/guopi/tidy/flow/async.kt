package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import java.util.concurrent.atomic.AtomicBoolean

fun <R> AsyncPlane.flow(action: (s: AsyncSubscriber<R>) -> Unit): Flow<R> {
    return Flow<R> { subscriber ->
        AsyncTask(this, subscriber, action).submit()
    }
}

class AsyncTask<T>(
    private val plane: AsyncPlane,
    private val subscriber: FSubscriber<T>,
    private val action: (s: AsyncSubscriber<T>) -> Unit,
) : AsyncSubscriber<T>, FSubscription, AtomicBoolean(), Runnable {
    private var submitted: AsyncSubscription? = null

    @MustCallInMainPlane
    override fun cancel() {
        if (this.compareAndSet(false, true)) {
            submitted?.let { s ->
                submitted = null
                plane.start {
                    s.cancelAsync()
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

    @MustCallInMainPlane
    fun submit() {
        submitted = plane.submit(this)
        subscriber.onSubscribe(this)
    }

    @MustCallInAsyncPlane
    override fun onAsyncValue(v: T) {
        if (get()) return

        Tidy.main.start { subscriber.onValue(v) }
    }

    @MustCallInAsyncPlane
    override fun onAsyncComplete() {
        if (get()) return

        Tidy.main.start { subscriber.onComplete() }
    }

    @MustCallInAsyncPlane
    override fun onAsyncError(e: Throwable) {
        if (get()) return

        Tidy.main.start { subscriber.onError(e) }
    }
}