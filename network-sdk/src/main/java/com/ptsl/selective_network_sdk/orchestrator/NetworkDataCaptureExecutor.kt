package com.ptsl.selective_network_sdk.orchestrator

import com.ptsl.selective_network_sdk.utils.Constants
import com.ptsl.selective_network_sdk.workers.DataCapturer
import com.ptsl.selective_network_sdk.workers.DataEnricher
import com.ptsl.selective_network_sdk.workers.PerformanceTester
import com.ptsl.selective_network_sdk.workers.PreFlightValidator
import com.ptsl.selective_network_sdk.data_model.FWAAssessmentExecutionInput
import com.ptsl.selective_network_sdk.data_model.NetworkMetrics
import com.ptsl.selective_network_sdk.data_model.NetworkDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestration Layer: Coordinates the step-by-step logic of the measurement process.
 * Fully stateless: No local database persistence.
 */
class NetworkDataCaptureExecutor(
    private val validator: PreFlightValidator,
    private val performanceTester: PerformanceTester,
    private val dataCapturer: DataCapturer,
    private val dataEnricher: DataEnricher
) {
    /**
     * Executes the measurement flow.
     */
    suspend fun execute(input: FWAAssessmentExecutionInput): NetworkDataResponse = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Pre-Flight Validation
            val validationError = validator.validate()

            if (validationError != null) {
                return@withContext NetworkDataResponse(
                    status = Constants.STATUS_FAILED,
                    statusCode = validationError.second,
                    message = validationError.first
                )
            }

            // 2. Performance Testing
            val metrics = performanceTester.testLatencyAndRtt()
            // 3. Primary Data Capture
            val rawData = dataCapturer.captureData( metrics)

            // 4. Data Enrichment
            val enrichedData = dataEnricher.enrich(
                rawDataList = rawData,
                msisdn = input.msisdn,
                integratedAppVersion = input.integratedAppVersion,
                sdkInitiateTimeStamp = input.sdkInitiateTimeStamp,
                integratedAppEventName = input.integratedAppEventName,
                userLatitude = input.userLatitude,
                userLongitude = input.userLongitude
            )

            // 5. Result Mapping
            NetworkDataResponse(
                status = Constants.STATUS_SUCCESS,
                statusCode = 200,
                message = "Measurement completed successfully.",
                testResult = Constants.RESULT_SUCCESS,
                data = enrichedData
            )

        } catch (e: Exception) {
            NetworkDataResponse(
                status = Constants.STATUS_FAILED,
                statusCode = 500,
                message = "Execution error: ${e.message}",
                testResult = Constants.RESULT_FAILED
            )
        }
    }
}
