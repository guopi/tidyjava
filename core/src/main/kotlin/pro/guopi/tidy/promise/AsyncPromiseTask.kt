package pro.guopi.tidy.promise

import pro.guopi.tidy.*
import pro.guopi.tidy.Tidy.Companion.main

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
        main.start {
            mainSubscriber.onSuccess(v)
        }
    }

    @MustCallInAsyncPlane
    override fun onAsyncError(e: Throwable) {
        main.start {
            mainSubscriber.onError(e)
        }
    }
}