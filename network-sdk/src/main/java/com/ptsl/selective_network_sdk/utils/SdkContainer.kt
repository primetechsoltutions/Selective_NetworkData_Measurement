package com.ptsl.selective_network_sdk.utils

import android.content.Context
import android.util.Log
import com.ptsl.selective_network_sdk.api.ApiService
import com.ptsl.selective_network_sdk.api.NetworkModule
import com.ptsl.selective_network_sdk.dl_ul_test.DownloadUploadHelper
import com.ptsl.selective_network_sdk.orchestrator.NetworkDataCaptureExecutor
import com.ptsl.selective_network_sdk.provider.*
import com.ptsl.selective_network_sdk.workers.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Lightweight manual DI container for the SDK.
 * Cleaned up with better separation of concerns using Providers and Mappers.
 */
internal object SdkContainer {
    private const val TAG = "SdkContainer"

    // ─── Core infrastructure ─────────────────────────────────────────────────

    var coroutineScope: CoroutineScope? = null
        private set
    var apiService: ApiService? = null
        private set
    var downloadUploadHelper: DownloadUploadHelper? = null
        private set
    var networkStateProvider: NetworkStateProvider? = null
        private set
    var simInfoProvider: SimInfoProvider? = null
        private set

    // ─── Modular Components ──────────────────────────────────────────────────
    
    var preFlightValidator: PreFlightValidator? = null
        private set
    var performanceTester: PerformanceTester? = null
        private set
    var dataCapturer: DataCapturer? = null
        private set
    var dataEnricher: DataEnricher? = null
        private set
    var cellDataMapper: CellDataMapper? = null
        private set
    var networkDataCaptureExecutor: NetworkDataCaptureExecutor? = null
        private set

    // ─── State ───────────────────────────────────────────────────────────────

    @Volatile
    private var initialized = false

    fun isInitialized(): Boolean = initialized &&
            coroutineScope      != null &&
            apiService          != null &&
            downloadUploadHelper != null &&
            networkStateProvider != null &&
            simInfoProvider      != null &&
            networkDataCaptureExecutor != null

    // ─── Init ────────────────────────────────────────────────────────────────

    @Synchronized
    fun init(context: Context) {
        if (isInitialized()) {
            Log.i(TAG, "Already initialized, skipping re-initialization")
            return
        }

        try {
            val appContext = context.applicationContext

            // 1. Coroutine scope
            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                Log.e(TAG, "Coroutine error: ${exception.message}")
            }
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

            // 2. Network layer
            val currentApiService = NetworkModule.apiService
            apiService            = currentApiService
            val currentDownloader = DownloadUploadHelper(currentApiService)
            downloadUploadHelper  = currentDownloader

            // 3. State providers
            networkStateProvider = NetworkStateProviderImpl(appContext)
            simInfoProvider = SimInfoProviderImpl(appContext)

            // 4. Mappers
            cellDataMapper = CellDataMapper(currentDownloader)

            // 5. Modular Workers
            val metricsHelper = NetworkMetricsHelper(NetworkModule.provideOkHttpClient())
            val timeProvider = DateTimeProviderImpl()
            preFlightValidator = PreFlightValidator(appContext, networkStateProvider!!, simInfoProvider!!)
            performanceTester = PerformanceTester(metricsHelper)
            dataCapturer = DataCapturer(appContext, networkStateProvider!!, simInfoProvider!!, cellDataMapper!!, timeProvider)
            dataEnricher = DataEnricher()

            // 6. Orchestrator
            networkDataCaptureExecutor = NetworkDataCaptureExecutor(
                preFlightValidator!!,
                performanceTester!!,
                dataCapturer!!,
                dataEnricher!!
            )

            initialized = true
            Log.i(TAG, "SdkContainer initialized successfully")
        } catch (e: Exception) {
            initialized = false
            Log.e(TAG, "Init failed: ${e.message}")
        }
    }
}