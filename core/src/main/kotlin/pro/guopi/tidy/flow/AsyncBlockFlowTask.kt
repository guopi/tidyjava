package pro.guopi.tidy.flow

import pro.guopi.tidy.*
import java.util.concurrent.atomic.AtomicBoolean

class AsyncBlockFlowTask<T>(
    private val plane: AsyncPlane,
    private val flowSubscriber: FlowSubscriber<T>,
    private val action: (s: AsyncBlockFlowSubscriber<T>) -> Unit,
) : AsyncBlockFlowSubscriber<T>, Subscription, AtomicBoolean(), Runnable {
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

        try {
            action(this)
        } catch (e: Throwable) {
            if (isCanceled()) {
                Tidy.onError(e)
            } else {
                Tidy.main.start {
                    flowSubscriber.onError(e)
                }
            }
            return
        }

        if (get()) return

        Tidy.main.start {
            flowSubscriber.onComplete()
        }
    }

    @MustCallInMainPlane
    fun submit() {
        submitted = plane.submit(this)
        flowSubscriber.onSubscribe(this)
    }

    @MustCallInAsyncPlane
    override fun onAsyncValue(value: T) {
        if (get()) return

        Tidy.main.start {
            flowSubscriber.onValue(value)
        }
    }
}