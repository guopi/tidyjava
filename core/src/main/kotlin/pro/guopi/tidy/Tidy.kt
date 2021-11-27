package pro.guopi.tidy

import pro.guopi.tidy.promise.AsyncPromiseSubscriber
import pro.guopi.tidy.promise.AsyncPromiseTask
import pro.guopi.tidy.promise.StdPromise
import java.util.concurrent.TimeUnit

class Tidy {
    companion object {
        @JvmStatic
        private val main = BasePlane("Tidy-Main", 1, 1)

        @JvmStatic
        val io = AsyncThreadPoolPlane("Tidy-Io", 0, Int.MAX_VALUE)

        @JvmStatic
        val computation = AsyncThreadPoolPlane(
            "Tidy-Comp",
            0, Runtime.getRuntime().availableProcessors()
        )

        @JvmStatic
        fun isInMainPlane(): Boolean {
            return main.isInPlane()
        }

        /**
         * run action in main plane
         */
        @JvmStatic
        fun runInMainPlane(action: Runnable) {
            main.safeRunInPlane(action)
        }

        @JvmStatic
        fun runLater(action: () -> Unit) {
            main.safeSchedule(0, TimeUnit.NANOSECONDS, action)
        }

        @JvmStatic
        fun runDelay(delay: Long, unit: TimeUnit, action: Runnable) {
            main.safeSchedule(delay, unit, action)
        }

        @JvmStatic
        fun <R> asyncRun(plane: AsyncPlane, action: () -> R): Promise<R> {
            return asyncRun(plane) { s ->
                try {
                    val r = action()
                    s.onAsyncValue(r)
                } catch (e: Throwable) {
                    s.onAsyncError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncRun(
            plane: AsyncPlane,
            action: (AsyncPromiseSubscriber<R>) -> Unit,
        ): Promise<R> {
            val promise = StdPromise<R>()
            AsyncPromiseTask(promise, action).submit(plane)
            return promise
        }

        @JvmStatic
        fun <R> asyncFlow(plane: AsyncPlane, action: (s: AsyncSubscriber<R>) -> Unit): YFlow<R> {
            return YFlow<R> { ys ->
                AsyncTask(plane, ys, action).submit()
            }
        }

        @JvmStatic
        fun setMaxComputationThreadCount(max: Int) {
            computation.setMaxThreadCount(max)
        }
    }
}

