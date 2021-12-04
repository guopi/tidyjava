@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy

interface ErrorSubscriber {
    @MustCallInMainPlane
    fun onError(error: Throwable)
}

fun ErrorSubscriber?.safeOnError(e: Throwable) {
    (this ?: Tidy).onError(e)
}

inline fun Throwable.safeAddSuppressed(error:Throwable): Throwable {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    (this as java.lang.Throwable).addSuppressed(error)
    return this
}

inline fun runOrHandleError(action: Runnable) {
    try {
        action.run()
    } catch (e: Throwable) {
        Tidy.onError(e)
    }
}

class SafeRunnable(val action: Runnable) : Runnable {
    override fun run() {
        runOrHandleError(action)
    }
}
