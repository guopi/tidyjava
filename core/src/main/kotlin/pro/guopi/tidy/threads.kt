package pro.guopi.tidy

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


open class ThreadPool(
    val name: String, minThreadCount: Int, maxThreadCount: Int,
) : ThreadFactory {
    val group = ThreadGroup(name)
    private val threadSN = AtomicInteger()

    protected val pool = ScheduledThreadPoolExecutor(minThreadCount, this)
        .also {
            it.maximumPoolSize = maxThreadCount
        }

    fun isRunningInPool(): Boolean {
        return Thread.currentThread().threadGroup === group
    }

    override fun newThread(r: Runnable): Thread {
        return Thread(group, "$name-${threadSN.getAndIncrement()}")
    }

    @CallInAnyPlane
    fun safeRunInPool(action: () -> Unit) {
        if (isRunningInPool()) {
            safeRun(action)
        } else {
            pool.execute(SafeRunnable(action))
        }
    }

    @CallInAnyPlane
    fun safeRunInPool(action: Runnable) {
        if (isRunningInPool()) {
            safeRun(action)
        } else {
            pool.execute(SafeRunnable(action))
        }
    }

    @CallInAnyPlane
    fun safeRunLater(action: Runnable) {
        pool.execute(SafeRunnable(action))
    }
}

class SchedulerThreadPoolPlane(
    name: String, minThreadCount: Int, maxThreadCount: Int,
) : AsyncPlane, ThreadPool(name, minThreadCount, maxThreadCount) {
    @CallInAnyPlane
    override fun start(action: Runnable) {
        safeRunInPool(action)
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