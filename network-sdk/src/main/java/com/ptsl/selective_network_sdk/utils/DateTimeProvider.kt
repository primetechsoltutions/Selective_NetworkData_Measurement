package com.ptsl.selective_network_sdk.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Clean interface for providing current date and time.
 * Makes the code more testable by allowing mocks for time.
 */
interface DateTimeProvider {
    fun getCurrentDateTime(): String
    fun getCurrentDate(): String
}

class DateTimeProviderImpl : DateTimeProvider {
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getCurrentDateTime(): String = dateTimeFormat.format(Date())
    override fun getCurrentDate(): String = dateOnlyFormat.format(Date())
}
