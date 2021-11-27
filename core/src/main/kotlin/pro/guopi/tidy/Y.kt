package pro.guopi.tidy

import java.util.concurrent.TimeUnit

class Y {
    companion object {
        @JvmStatic
        private val main = ThreadPool("Tidy-Main", 1, 1)

        @JvmStatic
        val io = SchedulerThreadPoolPlane("Tidy-Io", 0, Int.MAX_VALUE)

        @JvmStatic
        val computation = SchedulerThreadPoolPlane(
            "Tidy-Comp",
            0, Runtime.getRuntime().availableProcessors()
        )

        @JvmStatic
        fun isInMainPlane(): Boolean {
            return main.isRunningInPool()
        }

        /**
         * run action in main plane
         */
        @JvmStatic
        fun runInMainPlane(action: Runnable) {
            main.safeRunInPool(action)
        }

        @JvmStatic
        fun runInMainPlaneLater(action: () -> Unit) {
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
            action: FnPromiseCreateAction<R>,
        ): Promise<R> {
            return YWish { ys ->
                AsyncTask(plane, ys, action).submit()
            }
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

