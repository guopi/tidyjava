package pro.guopi.tidy

class Y {
    companion object : ThreadPool("Tidy-Main", 1, 1) {
        @JvmStatic
        val io = SchedulerThreadPoolPlane("Tidy-Io", 0, Int.MAX_VALUE)

        @JvmStatic
        val computation = SchedulerThreadPoolPlane(
            "Tidy-Comp",
            0, Runtime.getRuntime().availableProcessors()
        )

        @JvmStatic
        fun isInMainPlane(): Boolean {
            return isRunningInPool()
        }

        /**
         * run action in main plane
         */
        @JvmStatic
        fun runInMainPlane(action: () -> Unit) {
            if (isInMainPlane())
                action()
            else
                pool.execute(action)
        }

        @JvmStatic
        fun submitToMainPlane(action: () -> Unit) {
            pool.execute(action)
        }


        @JvmStatic
        fun <R> asyncWish(plane: AsyncPlane, action: () -> R): YWish<R> {
            return asyncWish(plane) { s ->
                try {
                    val r = action()
                    s.onAsyncValue(r)
                } catch (e: Throwable) {
                    s.onAsyncError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncWish(
            plane: AsyncPlane,
            action: (asyncSubscriber: AsyncSubscriber<R>) -> Unit
        ): YWish<R> {
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

