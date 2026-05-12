package com.ptsl.selective_network_sdk.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.coroutines.coroutineContext

/**
 * Utility extensions for networking operations.
 */

suspend fun ResponseBody?.getTotalBytes(): Int {
    var size = 0
    val inputStream: InputStream? = this?.byteStream()
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var length: Int
    try {
        while (inputStream?.read(buffer).also { length = it ?: -1 } != -1) {
            coroutineContext.ensureActive()
            byteArrayOutputStream.write(buffer, 0, length)
        }
        val byteArray = byteArrayOutputStream.toByteArray()
        size = byteArray.size
    } catch (e: Exception) {
        if (e is CancellationException) throw e
    } finally {
        inputStream?.close()
        byteArrayOutputStream.close()
    }
    return size
}
