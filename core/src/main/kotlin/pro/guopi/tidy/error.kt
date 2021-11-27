@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy

fun FSubscriber<*>?.safeOnError(e: Throwable) {
    if (this !== null)
        this.onError(e)
    else
        Tidy.onError(e)
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

