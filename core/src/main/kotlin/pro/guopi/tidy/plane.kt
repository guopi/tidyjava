package pro.guopi.tidy

import java.lang.annotation.Inherited
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Inherited
annotation class MustCallInAsyncPlane

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Inherited
annotation class CallInAnyPlane

interface Plane {
    @CallInAnyPlane
    fun start(action: Runnable)

    @CallInAnyPlane
    fun startDelay(delay: Long, unit: TimeUnit, action: Runnable)
}

fun Plane.startLater(action: () -> Unit) {
    startDelay(0, TimeUnit.NANOSECONDS, action)
}

interface MainPlane : Plane {
    @CallInAnyPlane
    fun submit(action: Runnable): FSubscription
}

interface AsyncPlane : Plane {
    @CallInAnyPlane
    fun submit(action: Runnable): AsyncSubscription
}

interface AsyncSubscriber<in T> {
    @MustCallInAsyncPlane
    fun isCanceled(): Boolean

    @MustCallInAsyncPlane
    fun onAsyncValue(v: T)

    @MustCallInAsyncPlane
    fun onAsyncComplete()

    @MustCallInAsyncPlane
    fun onAsyncError(e: Throwable)
}

@FunctionalInterface
interface AsyncSubscription {
    @MustCallInAsyncPlane
    fun cancelAsync()
}

