package pro.guopi.tidy.flow

import pro.guopi.tidy.*

interface AsyncFlowSubscriber<in T> {
    @MustCallInAsyncPlane
    fun isCanceled(): Boolean

    @MustCallInAsyncPlane
    fun onAsyncValue(value: T)

    @MustCallInAsyncPlane
    fun onAsyncComplete()

    @MustCallInAsyncPlane
    fun onAsyncError(error: Throwable)
}

interface AsyncBlockFlowSubscriber<in T> {
    @MustCallInAsyncPlane
    fun isCanceled(): Boolean

    @MustCallInAsyncPlane
    fun onAsyncValue(value: T)
}


fun <R> AsyncPlane.asyncFlow(action: (s: AsyncFlowSubscriber<R>) -> Unit): Flowable<R> {
    return Flowable<R> { subscriber ->
        AsyncFlowTask(this, subscriber, action).submit()
    }
}

fun <R> AsyncPlane.blockFlow(action: (s: AsyncBlockFlowSubscriber<R>) -> Unit): Flowable<R> {
    return Flowable<R> { subscriber ->
        AsyncBlockFlowTask(this, subscriber, action).submit()
    }
}

