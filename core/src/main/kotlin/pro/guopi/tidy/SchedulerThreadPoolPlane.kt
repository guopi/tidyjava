package pro.guopi.tidy

import java.util.concurrent.ScheduledThreadPoolExecutor

class SchedulerThreadPoolPlane(
    internal val pool: ScheduledThreadPoolExecutor
) : YPlane {
}