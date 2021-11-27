package pro.guopi.tidy.promise

import pro.guopi.tidy.MustCallInAsyncPlane

interface AsyncPromiseSubscriber<in T> {
    @MustCallInAsyncPlane
    fun onAsyncValue(v: T)

    @MustCallInAsyncPlane
    fun onAsyncError(e: Throwable)
}
