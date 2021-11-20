package pro.guopi.tidy

class Y {
    companion object {
        @JvmStatic
        private val mainThreads =
            createScheduleThreadPool(1, 1)

        @JvmStatic
        val io: SchedulerThreadPoolPlane =
            SchedulerThreadPoolPlane(
                createScheduleThreadPool(0, Int.MAX_VALUE)
            )

        @JvmStatic
        val computation: SchedulerThreadPoolPlane =
            SchedulerThreadPoolPlane(
                createScheduleThreadPool(0, Runtime.getRuntime().availableProcessors())
            )

        /**
         * run action in main plane
         */
        @JvmStatic
        fun start(action: () -> Unit) {
            mainThreads.execute(action)
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
    }
}

