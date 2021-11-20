package pro.guopi.tidy

fun YSubscriber<*>?.handleError(e: Throwable) {
    if (this !== null)
        this.onError(e)
    else
        YErrors.ON_ERROR_DEFAULT(e)
}

object YErrors {
    @JvmStatic
    var ON_ERROR_DEFAULT: FnOnError = ::defaultOnError
        private set

    @JvmStatic
    fun setDefaultOnError(onError: FnOnError) {
        ON_ERROR_DEFAULT = onError
    }

    @JvmStatic
    fun defaultOnError(error: Throwable) {
        Y.io.start {
            error.printStackTrace()
        }
        uncaught(error)
    }

    @JvmStatic
    internal fun uncaught(error: Throwable) {
        val thread = Thread.currentThread()
        thread.uncaughtExceptionHandler.uncaughtException(thread, error)
    }

    @JvmStatic
    fun handleSubscriptionAlreadySet(current: YSubscription, new: YSubscription) {
        new.cancel()
        if (current !== YSubscription.TERMINATED) {
            defaultOnError(IllegalStateException("Subscription already set!"))
        }
    }
}