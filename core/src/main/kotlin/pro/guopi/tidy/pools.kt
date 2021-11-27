package pro.guopi.tidy

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


open class BasePlane(
    val name: String, minThreadCount: Int, maxThreadCount: Int,
) : ThreadFactory {
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
    fun safeRunInPlane(action: Runnable) {
        if (isInPlane()) {
            safeRun(action)
        } else {
            pool.execute(SafeRunnable(action))
        }
    }

    @CallInAnyPlane
    fun safeSchedule(delay: Long, unit: TimeUnit, action: Runnable) {
        pool.schedule(SafeRunnable(action), delay, unit)
    }
}

class AsyncThreadPoolPlane(
    name: String, minThreadCount: Int, maxThreadCount: Int,
) : AsyncPlane, BasePlane(name, minThreadCount, maxThreadCount) {

    @CallInAnyPlane
    override fun start(action: Runnable) {
        safeRunInPlane(action)
    }

    @CallInAnyPlane
    override fun submit(action: Runnable): AsyncSubscription {
        val f = pool.submit(SafeRunnable(action))
        return AsyncSubscription {
            f.cancel(false)
        }
    }

    fun setMaxThreadCount(max: Int) {
        pool.maximumPoolSize = max
    }
}