@file:Suppress("NOTHING_TO_INLINE")

package pro.guopi.tidy

@FunctionalInterface
interface Subscription {
    @MustCallInMainPlane
    fun cancel()

    companion object {
        @JvmStatic
        fun cannotOnSubscribe(current: Subscription?, new: Subscription) {
            new.cancel()
            if (current !== null) {
                Tidy.onError(IllegalStateException("Subscription already set!"))
            }
        }
    }
}