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
                    s.onValue(r)
                } catch (e: Throwable) {
                    s.onError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncStart(plane: YPlane, action: (s: YSubscriber<R>) -> Unit): YFuture<R> {
            return asyncLazyStart(plane, action).cache()
        }

        @JvmStatic
        fun <R> asyncLazyCall(plane: YPlane, action: () -> R): YFuture<R> {
            return asyncLazyStart(plane) { s ->
                try {
                    val r = action()
                    s.onValue(r)
                } catch (e: Throwable) {
                    s.onError(e)
                }
            }
        }

        @JvmStatic
        fun <R> asyncLazyStart(plane: YPlane, action: (s: YSubscriber<R>) -> Unit): YFuture<R> {
            return object : YFuture<R> {
                override fun subscribe(mainSub: YSubscriber<R>) {
                    plane.submit {
                        action(object : YSubscriber<R> {
                            override fun onSubscribe(s: YSubscription) {
                                start { mainSub.onSubscribe(s) }
                            }

                            override fun onValue(v: R) {
                                start { mainSub.onValue(v) }
                            }

                            override fun onComplete() {
                                start { mainSub.onComplete() }
                            }

                            override fun onError(e: Throwable) {
                                start { mainSub.onError(e) }
                            }
                        })
                    }
                }
            }
        }

        @JvmStatic
        fun <R> asyncFlow(plane: YPlane, action: (s: YSubscriber<R>) -> Unit): YFlow<R> {
            TODO()
        }

        @JvmStatic
        fun <R> asyncLazyFlow(plane: YPlane, action: (s: YSubscriber<R>) -> Unit): YFlow<R> {
            TODO()
        }
    }
}
