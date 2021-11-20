package pro.guopi.tidy

import pro.guopi.tidy.op.cache

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

        @JvmStatic
        fun start(action: () -> Unit) {
            mainThreads.execute(action)
        }

        @JvmStatic
        fun <R> asyncCall(plane: YPlane, action: () -> R): YFuture<R> {
            return asyncStart(plane) { s ->
                try {
                    val r = action()
                    s.onAsyncValue(r)
                } catch (e: Throwable) {
                    s.onAsyncError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncStart(plane: YPlane, action: (s: AsyncSubscriber<R>) -> Unit): YFuture<R> {
            return asyncLazyStart(plane, action).cache()
        }

        @JvmStatic
        fun <R> asyncLazyCall(plane: YPlane, action: () -> R): YFuture<R> {
            return asyncLazyStart(plane) { s ->
                try {
                    val r = action()
                    s.onAsyncValue(r)
                } catch (e: Throwable) {
                    s.onAsyncError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncLazyStart(
            plane: YPlane,
            action: (asyncSubscriber: AsyncSubscriber<R>) -> Unit
        ): YFuture<R> {
            return object : YFuture<R> {
                override fun subscribe(s: YSubscriber<R>) {
                    plane.submit {
                        action(object : AsyncSubscriber<R> {
                            override fun onAsyncSubscribe(subscription: YSubscription) {
                                start { s.onSubscribe(subscription) }
                            }

                            override fun onAsyncValue(v: R) {
                                start { s.onValue(v) }
                            }

                            override fun onAsyncComplete() {
                                start { s.onComplete() }
                            }

                            override fun onAsyncError(e: Throwable) {
                                start { s.onError(e) }
                            }
                        })
                    }
                }
            }
        }

        @JvmStatic
        fun <R> asyncFlow(plane: YPlane, action: (s: AsyncSubscriber<R>) -> Unit): YFlow<R> {
            return object : YFlow<R> {
                override fun subscribe(s: YSubscriber<R>) {
                    plane.submit {
                        action(object : AsyncSubscriber<R> {
                            override fun onAsyncSubscribe(subscription: YSubscription) {
                                start { s.onSubscribe(subscription) }
                            }

                            override fun onAsyncValue(v: R) {
                                start { s.onValue(v) }
                            }

                            override fun onAsyncComplete() {
                                start { s.onComplete() }
                            }

                            override fun onAsyncError(e: Throwable) {
                                start { s.onError(e) }
                            }
                        })
                    }
                }
            }
        }
    }
}
