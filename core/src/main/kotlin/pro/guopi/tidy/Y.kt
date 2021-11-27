package pro.guopi.tidy

class Y {
    companion object {
        @JvmStatic
        val main = ThreadPool("Tidy-Main", 1, 1)

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
            main.safeRunLater(action)
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
            action: FnPromiseCreateAction<R>
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
            computation.pool.maximumPoolSize = max
        }


    }
}

