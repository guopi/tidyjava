package pro.guopi.tidy.promise

import pro.guopi.tidy.PromiseSubscriber

class TerminatableSubsciber<T>(
    val downStream: PromiseSubscriber<T>,
) : PromiseSubscriber<T> {
    var terminated = false

    fun terminate(): Boolean {
        return if (!terminated) {
            terminated = true
            true
        } else {
            false
        }
    }

    override fun onSuccess(value: T) {
        if (terminate()) {
            downStream.onSuccess(value)
        }
    }


    override fun onError(error: Throwable) {
        if (terminate()) {
            downStream.onError(error)
        }
    }
}

