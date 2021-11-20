package pro.guopi.tidy

import java.util.concurrent.ScheduledThreadPoolExecutor

class SchedulerThreadPoolPlane(
    val pool: ScheduledThreadPoolExecutor
) : AsyncPlane {
    @CallInAnyPlane
    override fun start(action: Runnable) {
        pool.execute(action)
    }

    @CallInAnyPlane
    override fun submit(action: Runnable): AsyncSubscription {
        val f = pool.submit(action)
        return AsyncSubscription {
            f.cancel(false)
        }
    }
}