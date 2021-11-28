@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy

@FunctionalInterface
interface Subscription {
    @MustCallInMainPlane
    fun cancel()

    companion object {
        val TERMINATED = object : Subscription {
            override fun cancel() {
                //DO NOTHING
            }
        }

        @JvmStatic
        fun handleSubscriptionAlreadySet(current: Subscription, new: Subscription) {
            new.cancel()
            if (current !== TERMINATED) {
                Tidy.onError(IllegalStateException("Subscription already set!"))
            }
        }
    }
}