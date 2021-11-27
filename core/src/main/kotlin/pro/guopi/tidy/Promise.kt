package pro.guopi.tidy

import pro.guopi.tidy.promise.NormalPromise


interface PromiseSubscriber<T> {
    @MustCallInMainPlane
    fun onSuccess(v: T)

    @MustCallInMainPlane
    fun onError(error: Throwable)
}

interface Promise<T> {
    @MustCallInMainPlane
    fun subscribe(subscriber: PromiseSubscriber<T>)

    fun <R> fastMap(
        onSuccess: (T) -> R,
        onError: ((Throwable) -> R)? = null
    ): Promise<R>? = null

    fun <R> fastThen(
        onSuccess: (T) -> Promise<R>,
        onError: ((Throwable) -> Promise<R>)? = null
    ): Promise<R>? = null


    companion object {
        @JvmStatic
        fun <T> create(action: (PromiseSubscriber<T>) -> Unit): Promise<T> {
            val promise = NormalPromise<T>()
            Y.runInMainPlane {
                action(promise)
            }
            return promise
        }

        @JvmStatic
        fun <T> success(v: T): Promise<T> {
            TODO()
        }

        @JvmStatic
        fun <T> error(e: Throwable): Promise<T> {
            TODO()
        }
    }
}