package com.ptsl.selective_network_sdk.workers

import com.ptsl.selective_network_sdk.data_model.NetworkMetrics
import com.ptsl.selective_network_sdk.utils.NetworkMetricsHelper

class PerformanceTester(private val metricsHelper: NetworkMetricsHelper) {
    suspend fun testLatencyAndRtt(): NetworkMetrics {
        val testUrl = "https://crsrcgz.banglalink.net"
        return metricsHelper.calculateMetrics(
            hasMobileInternet = true,
            testUrl = testUrl
        )
    }
}
