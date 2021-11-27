package pro.guopi.tidy.plane

import pro.guopi.tidy.CallInAnyPlane
import pro.guopi.tidy.Plane
import pro.guopi.tidy.SafeRunnable
import pro.guopi.tidy.runOrHandleError
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

open class ThreadPoolPlane(
    val name: String, minThreadCount: Int, maxThreadCount: Int,
) : ThreadFactory, Plane {
    val group = ThreadGroup(name)
    private val threadSN = AtomicInteger()

    protected val pool = ScheduledThreadPoolExecutor(minThreadCount, this)
        .also {
            it.maximumPoolSize = maxThreadCount
        }

    fun isInPlane(): Boolean {
        return Thread.currentThread().threadGroup === group
    }

    override fun newThread(r: Runnable): Thread {
        return Thread(group, "$name-${threadSN.getAndIncrement()}")
    }

    @CallInAnyPlane
    override fun start(action: Runnable) {
        if (isInPlane()) {
            runOrHandleError(action)
        } else {
            pool.execute(SafeRunnable(action))
        }
    }

    @CallInAnyPlane
    override fun startDelay(delay: Long, unit: TimeUnit, action: Runnable) {
        pool.schedule(SafeRunnable(action), delay, unit)
    }
}
