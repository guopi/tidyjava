package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import java.util.concurrent.atomic.AtomicBoolean

class AsyncFlowTask<T>(
    private val plane: AsyncPlane,
    private val flowSubscriber: FlowSubscriber<T>,
    private val action: (s: AsyncFlowSubscriber<T>) -> Unit,
) : AsyncFlowSubscriber<T>, Subscription, AtomicBoolean(), Runnable {
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
        flowSubscriber.onSubscribe(this)
    }

    @MustCallInAsyncPlane
    override fun onAsyncValue(v: T) {
        if (get()) return

        Tidy.main.start { flowSubscriber.onValue(v) }
    }

    @MustCallInAsyncPlane
    override fun onAsyncComplete() {
        if (get()) return

        Tidy.main.start {
            flowSubscriber.onComplete()
        }
    }

    @MustCallInAsyncPlane
    override fun onAsyncError(e: Throwable) {
        if (get()) return

        Tidy.main.start {
            flowSubscriber.onError(e)
        }
    }
}