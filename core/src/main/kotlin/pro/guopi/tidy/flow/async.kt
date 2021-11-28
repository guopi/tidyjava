package pro.guopi.tidy.flow

import pro.guopi.tidy.*

interface AsyncFlowSubscriber<in T> {
    @MustCallInAsyncPlane
    fun isCanceled(): Boolean

    @MustCallInAsyncPlane
    fun onAsyncValue(v: T)

    @MustCallInAsyncPlane
    fun onAsyncComplete()

    @MustCallInAsyncPlane
    fun onAsyncError(e: Throwable)
}


fun <R> AsyncPlane.flow(action: (s: AsyncFlowSubscriber<R>) -> Unit): Flowable<R> {
    return Flowable<R> { subscriber ->
        AsyncFlowTask(this, subscriber, action).submit()
    }
}

