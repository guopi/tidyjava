package pro.guopi.tidy.io

import pro.guopi.tidy.Flowable
import pro.guopi.tidy.Tidy
import pro.guopi.tidy.flow.AsyncBlockFlowSubscriber
import pro.guopi.tidy.flow.blockFlow
import java.io.BufferedReader
import java.io.Reader

fun Reader.linesFlow(): Flowable<String> {
    return Tidy.io.blockFlow {
        buffered().blockingReadLines(it)
    }
}

fun BufferedReader.blockingReadLines(subscriber: AsyncBlockFlowSubscriber<String>) {
    this.use { reader ->
        while (!subscriber.isCanceled()) {
            val line = reader.readLine()
            if (line === null)
                break
            subscriber.onAsyncValue(line)
        }
    }
}