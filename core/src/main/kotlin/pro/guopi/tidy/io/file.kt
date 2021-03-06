package pro.guopi.tidy.io

import pro.guopi.tidy.Flowable
import pro.guopi.tidy.Promise
import pro.guopi.tidy.Tidy
import pro.guopi.tidy.flow.blockFlow
import pro.guopi.tidy.promise.asyncCall
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


fun File.readTextPromise(charset: Charset = Charsets.UTF_8): Promise<String> {
    return Tidy.io.asyncCall {
        this.readText(charset)
    }
}

fun File.linesFlow(charset: Charset = Charsets.UTF_8): Flowable<String> {
    return Tidy.io.blockFlow {
        BufferedReader(InputStreamReader(FileInputStream(this), charset))
            .blockingReadLines(it)
    }
}