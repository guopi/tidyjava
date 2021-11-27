package pro.guopi.tidy.promise

import pro.guopi.tidy.*

class AsyncPromiseTask<T>(
    private val mainSubscriber: PromiseSubscriber<T>,
    private val action: (asyncSubscriber: AsyncPromiseSubscriber<T>) -> Unit,
) : AsyncPromiseSubscriber<T>, Runnable {

    @CallInAnyPlane
    fun submit(plane: AsyncPlane) {
        plane.submit(this)
    }

    override fun run() {
        action(this)
    }

    @MustCallInAsyncPlane
    override fun onAsyncValue(v: T) {
        Tidy.runInMainPlane {
            mainSubscriber.onSuccess(v)
        }
    }

    @MustCallInAsyncPlane
    override fun onAsyncError(e: Throwable) {
        Tidy.runInMainPlane {
            mainSubscriber.onError(e)
        }
    }
}