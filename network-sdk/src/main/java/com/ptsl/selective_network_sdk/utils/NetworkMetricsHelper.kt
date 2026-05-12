package com.ptsl.selective_network_sdk.utils

import com.ptsl.selective_network_sdk.data_model.NetworkMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * Optimized helper for calculating Network Metrics (RTT and Latency).
 * Uses a single OkHttpClient and EventListener for precise measurements.
 */
class NetworkMetricsHelper(private val okHttpClient: OkHttpClient) {

    suspend fun calculateMetrics(
        testUrl: String,
        hasMobileInternet: Boolean,
        samples: Int = 3
    ): NetworkMetrics = withContext(Dispatchers.IO) {

        if (!hasMobileInternet) return@withContext NetworkMetrics(0.0, 0.0)

        val rttSamples = mutableListOf<Long>()
        val latencySamples = mutableListOf<Long>()

        val url = testUrl.toHttpUrlOrNull() ?: return@withContext NetworkMetrics(0.0, 0.0)
        val host = url.host
        val port = url.port
        val isIpAddress = host.matches(Regex("""\d{1,3}(\.\d{1,3}){3}"""))

        repeat(samples) {
            measureSample(testUrl, host, port, isIpAddress, rttSamples, latencySamples)
        }

        NetworkMetrics(
            rtt = calculateMedian(rttSamples),
            latency = calculateMedian(latencySamples)
        )
    }

    private fun measureSample(
        testUrl: String,
        host: String,
        port: Int,
        isIpAddress: Boolean,
        rttSamples: MutableList<Long>,
        latencySamples: MutableList<Long>
    ) {
        var connectStartNs = 0L
        var connectEndNs = 0L
        var requestEndNs = 0L
        var responseStartNs = 0L
        var serverProcessingMs = 0L

        val listener = object : EventListener() {
            override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
                connectStartNs = System.nanoTime()
            }
            override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
                connectEndNs = System.nanoTime()
            }
            override fun requestHeadersEnd(call: Call, request: Request) {
                requestEndNs = System.nanoTime()
            }
            override fun responseHeadersStart(call: Call) {
                responseStartNs = System.nanoTime()
            }
        }

        val client = okHttpClient.newBuilder()
            .eventListener(listener)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            .build()

        try {
            val request = Request.Builder()
                .url(testUrl)
                .header("Connection", "close")
                .header("Host", host)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                response.header("X-Server-Processing-Time")?.let {
                    serverProcessingMs = it.toLongOrNull() ?: 0L
                }
            }

            // ---------- RTT ----------
            val rttMs = when {
                connectStartNs > 0 && connectEndNs > connectStartNs ->
                    (connectEndNs - connectStartNs) / 1_000_000
                requestEndNs > 0 && responseStartNs > requestEndNs ->
                    ((responseStartNs - requestEndNs) / 2) / 1_000_000
                else -> 0L
            }

            // ---------- Latency (TTFB) ----------
            val rawLatencyMs = if (requestEndNs > 0 && responseStartNs > requestEndNs)
                (responseStartNs - requestEndNs) / 1_000_000
            else 0L

            val latencyMs = (rawLatencyMs - serverProcessingMs).coerceAtLeast(0)

            if (rttMs > 0) rttSamples.add(rttMs)
            if (latencyMs > 0) latencySamples.add(latencyMs)

        } catch (_: Exception) {
            // TCP fallback for IP addresses if HTTP fails
            if (isIpAddress) {
                measureTcpFallback(host, port, rttSamples)
            }
        }
    }

    private fun measureTcpFallback(host: String, port: Int, rttSamples: MutableList<Long>) {
        try {
            val start = System.nanoTime()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 3000)
            }
            val end = System.nanoTime()
            rttSamples.add((end - start) / 1_000_000)
        } catch (_: Exception) { }
    }

    private fun calculateMedian(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0)
            (sorted[mid - 1] + sorted[mid]) / 2.0
        else sorted[mid].toDouble()
    }
}
