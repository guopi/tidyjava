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
        fun start(action: () -> Unit) {
            if (isInMainPlane())
                action()
            else
                pool.execute(action)
        }

        @JvmStatic
        fun startLater(action: () -> Unit) {
            pool.execute(action)
        }

        @JvmStatic
        fun <R> asyncCall(plane: AsyncPlane, action: () -> R): YPromise<R> {
            return asyncCall(plane) { s ->
                try {
                    val r = action()
                    s.onAsyncValue(r)
                } catch (e: Throwable) {
                    s.onAsyncError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncCall(plane: AsyncPlane, action: (s: AsyncSubscriber<R>) -> Unit): YPromise<R> {
            return asyncLazy(plane, action).promise()
        }

        @JvmStatic
        fun <R> asyncLazy(plane: AsyncPlane, action: () -> R): YFuture<R> {
            return asyncLazy(plane) { s ->
                try {
                    val r = action()
                    s.onAsyncValue(r)
                } catch (e: Throwable) {
                    s.onAsyncError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncLazy(
            plane: AsyncPlane,
            action: (asyncSubscriber: AsyncSubscriber<R>) -> Unit
        ): YFuture<R> {
            return YFuture { ys ->
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

