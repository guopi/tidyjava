package pro.guopi.tidy.flow

import pro.guopi.tidy.FlowSubscriber
import pro.guopi.tidy.Flowable
import pro.guopi.tidy.safeOnError

fun <T> Flowable<T>.takeFirst(): Flowable<T> {
    return Flowable { downstream ->
        this.subscribe(object : FilterSubscriber<T, T>(downstream) {
            override fun onValue(value: T) {
                ifUpStreamSubscribed {
                    cancelUpStreamWhenOperator()?.let {
                        it.onValue(value)
                        it.onComplete()
                    }
                }
            }
        })
    }
}

fun <T, R> Flowable<T>.takeFirstNotNull(mapper: (T) -> R?): Flowable<R> {
    return FlowTakeFirstMapNotNull(this, mapper)
}

class FlowTakeFirstMapNotNull<T, R>(
    private val source: Flowable<T>,
    private val mapper: (T) -> R?,
) : Flowable<R> {
    override fun subscribe(subscriber: FlowSubscriber<R>) {
        source.subscribe(object : FilterSubscriber<T, R>(subscriber) {
            override fun onValue(value: T) {
                ifUpStreamSubscribed {
                    try {
                        val r = mapper(value)
                        if (r !== null) {
                            cancelUpStreamWhenOperator()?.let {
                                it.onValue(r)
                                it.onComplete()
                            }
                        }
                    } catch (e: Throwable) {
                        cancelUpStreamWhenOperator().safeOnError(e)
                    }
                }
            }
        })
    }
}