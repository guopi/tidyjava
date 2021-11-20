package pro.guopi.tidy

import java.util.concurrent.ScheduledThreadPoolExecutor

fun createScheduleThreadPool(minThreadCount: Int, maxThreadCount: Int): ScheduledThreadPoolExecutor {
    return ScheduledThreadPoolExecutor(minThreadCount).also {
        it.maximumPoolSize = maxThreadCount
    }
}
