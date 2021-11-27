package pro.guopi.tidy.promise

import pro.guopi.tidy.AsyncPlane
import pro.guopi.tidy.Promise

fun <R> AsyncPlane.asyncCall(action: () -> R): Promise<R> {
    return this.asyncRun { s ->
        try {
            val r = action()
            s.onAsyncValue(r)
        } catch (e: Throwable) {
            s.onAsyncError(e)
        }
    }
}

fun <R> AsyncPlane.asyncRun(
    action: (AsyncPromiseSubscriber<R>) -> Unit,
): Promise<R> {
    val promise = StdPromise<R>()
    AsyncPromiseTask(promise, action).submit(this)
    return promise
}
