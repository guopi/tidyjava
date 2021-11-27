package pro.guopi.tidy.plane

import pro.guopi.tidy.AsyncPlane
import pro.guopi.tidy.AsyncSubscription
import pro.guopi.tidy.CallInAnyPlane
import pro.guopi.tidy.SafeRunnable
import java.util.concurrent.atomic.AtomicBoolean

class AsyncThreadPoolPlane(
    name: String, minThreadCount: Int, maxThreadCount: Int,
) : AsyncPlane, ThreadPoolPlane(name, minThreadCount, maxThreadCount) {

    @CallInAnyPlane
    override fun submit(action: Runnable): AsyncSubscription {
        val f = pool.submit(SafeRunnable(action))
        return object : AsyncSubscription, AtomicBoolean() {
            override fun cancelAsync() {
                if (compareAndSet(false, true))
                    f.cancel(false)
            }
        }
    }

    fun setMaxThreadCount(max: Int) {
        pool.maximumPoolSize = max
    }
}