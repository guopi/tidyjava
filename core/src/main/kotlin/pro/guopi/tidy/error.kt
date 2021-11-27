@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy

fun YSubscriber<*>?.handleError(e: Throwable) {
    if (this !== null)
        this.onError(e)
    else
        YErrors.ON_ERROR_DEFAULT(e)
}


inline fun safeRun(action: () -> Unit) {
    try {
        action()
    } catch (e: Throwable) {
        YErrors.handleError(e)
    }
}

inline fun safeRun(action: Runnable) {
    try {
        action.run()
    } catch (e: Throwable) {
        YErrors.handleError(e)
    }
}

class SafeRunnable(val action: Runnable) : Runnable {
    override fun run() {
        safeRun(action)
    }
}


object YErrors {
    @JvmStatic
    var ON_ERROR_DEFAULT: FnOnError = ::uncaught
        private set

    @JvmStatic
    fun setDefaultOnError(onError: FnOnError) {
        ON_ERROR_DEFAULT = onError
    }

    @JvmStatic
    fun <R> handleErrorUse(onError: ((Throwable) -> R)?, error: Throwable) {
        (onError ?: ON_ERROR_DEFAULT)(error)
    }

    @JvmStatic
    fun handleError(error: Throwable) {
        ON_ERROR_DEFAULT(error)
    }

    @JvmStatic
    internal fun uncaught(error: Throwable) {
        Y.io.start {
            error.printStackTrace()
        }

        val thread = Thread.currentThread()
        thread.uncaughtExceptionHandler.uncaughtException(thread, error)
    }

    @JvmStatic
    fun handleSubscriptionAlreadySet(current: YSubscription, new: YSubscription) {
        new.cancel()
        if (current !== YSubscription.TERMINATED) {
            handleError(IllegalStateException("Subscription already set!"))
        }
    }
}