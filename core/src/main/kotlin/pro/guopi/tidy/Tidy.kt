package pro.guopi.tidy

object Tidy {
    fun start(action: () -> Unit) {
    }

    fun <R> await(action: () -> R): TyPromise<R> {
        TODO()
    }

    fun <R> callComputation(computation: () -> R): TyPromise<R> {
        TODO()
    }
}
