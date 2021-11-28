package pro.guopi.tidy.plane

import pro.guopi.tidy.Subscription
import pro.guopi.tidy.MainPlane
import pro.guopi.tidy.MustCallInMainPlane
import pro.guopi.tidy.SafeRunnable

class MainThreadPoolPlane :
    ThreadPoolPlane("Tidy-Main", 1, 1),
    MainPlane {

    override fun submit(action: Runnable): Subscription {
        val f = pool.submit(SafeRunnable(action))
        return object : Subscription {
            private var canceled = false

            @MustCallInMainPlane
            override fun cancel() {
                if (canceled) return

                canceled = true
                f.cancel(false)
            }
        }
    }
}