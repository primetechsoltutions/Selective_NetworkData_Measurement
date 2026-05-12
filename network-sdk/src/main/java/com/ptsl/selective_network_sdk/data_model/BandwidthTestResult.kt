package com.ptsl.selective_network_sdk.data_model
data class BandwidthTestResult(
    val downloadSpeedKbps: Double = 0.0,
    val uploadSpeedKbps: Double = 0.0,
    val totalDownloadBytes: Long = 0,
    val totalUploadBytes: Long = 0
) {
    val totalDownloadMB: Double get() = totalDownloadBytes / (1024.0 * 1024.0)
    val totalUploadMB: Double get() = totalUploadBytes / (1024.0 * 1024.0)
}
