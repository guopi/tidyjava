package pro.guopi.tidy.promise

import pro.guopi.tidy.AsyncPlane
import pro.guopi.tidy.MustCallInAsyncPlane
import pro.guopi.tidy.Promise

interface AsyncPromiseSubscriber<in T> {
    @MustCallInAsyncPlane
    fun onAsyncValue(v: T)

    @MustCallInAsyncPlane
    fun onAsyncError(e: Throwable)
}

fun <R> AsyncPlane.asyncCall(action: () -> R): Promise<R> {
    return this.asyncPromise { s ->
        try {
            val r = action()
            s.onAsyncValue(r)
        } catch (e: Throwable) {
            s.onAsyncError(e)
        }
    }
}

fun <R> AsyncPlane.asyncPromise(
    action: (AsyncPromiseSubscriber<R>) -> Unit,
): Promise<R> {
    val promise = StdPromise<R>()
    AsyncPromiseTask(promise, action).submit(this)
    return promise
}
