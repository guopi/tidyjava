package pro.guopi.tidy

import pro.guopi.tidy.plane.AsyncThreadPoolPlane
import pro.guopi.tidy.plane.MainThreadPoolPlane

class Tidy {
    companion object {
        @JvmStatic
        val main: MainPlane = MainThreadPoolPlane()

        @JvmStatic
        val io: AsyncPlane = AsyncThreadPoolPlane("Tidy-Io", 0, Int.MAX_VALUE)

        @JvmStatic
        val computation: AsyncPlane = AsyncThreadPoolPlane(
            "Tidy-Comp",
            0, Runtime.getRuntime().availableProcessors()
        )

        @JvmStatic
        private var ON_ERROR_DEFAULT: FnOnError = ::uncaught

        @JvmStatic
        fun setOnError(onError: FnOnError) {
            ON_ERROR_DEFAULT = onError
        }

        @JvmStatic
        fun <R> handleErrorUse(onError: ((Throwable) -> R)?, error: Throwable) {
            (onError ?: ON_ERROR_DEFAULT)(error)
        }

        @JvmStatic
        fun onError(error: Throwable) {
            ON_ERROR_DEFAULT(error)
        }

        @JvmStatic
        private fun uncaught(error: Throwable) {
            io.start {
                error.printStackTrace()
            }

            val thread = Thread.currentThread()
            thread.uncaughtExceptionHandler.uncaughtException(thread, error)
        }
    }
}

