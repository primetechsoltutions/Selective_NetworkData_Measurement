package com.ptsl.selective_network_sdk

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.ptsl.selective_network_sdk.data_model.FWAAssessmentExecutionInput
import com.ptsl.selective_network_sdk.data_model.FWAMeasurementStatus
import com.ptsl.selective_network_sdk.data_model.NetworkDataResponse
import com.ptsl.selective_network_sdk.utils.CheckPermissionHandler
import com.ptsl.selective_network_sdk.utils.Constants
import com.ptsl.selective_network_sdk.utils.LifecycleCallbackDispatcher
import com.ptsl.selective_network_sdk.utils.SdkContainer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference

/**
 * Entry Layer (Facade): A single entry-point class for the host application.
 * Manages initialization and high-level execution triggers with lifecycle awareness.
 */
class NWNetworkDataMeasurement {
    private lateinit var checkPermissionHandler: CheckPermissionHandler
    private lateinit var context: Context
    private lateinit var applicationName: String
    private lateinit var callbackDispatcher: LifecycleCallbackDispatcher

    private val gson = Gson()

    companion object {
        private const val TAG = "MainManager"
        private const val EXECUTION_TIMEOUT_MS = 60_000L
    }

    /**
     * Initializes the SDK with the host activity.
     */
    fun init(activity: AppCompatActivity, applicationName: String) {
        setup(activity, activity, CheckPermissionHandler(activity), applicationName)
    }

    /**
     * Initializes the SDK with the host fragment.
     */
    fun init(fragment: Fragment, applicationName: String) {
        val activity = fragment.activity as? AppCompatActivity ?: return
        setup(activity, fragment, CheckPermissionHandler(fragment), applicationName)
    }

    private fun setup(
        activity: AppCompatActivity,
        owner: LifecycleOwner,
        permissionHandler: CheckPermissionHandler,
        appName: String
    ) {
        this.callbackDispatcher = LifecycleCallbackDispatcher(
            WeakReference(activity), WeakReference(owner)
        )
        this.checkPermissionHandler = permissionHandler
        this.context = activity.applicationContext
        this.applicationName = appName

        SdkContainer.init(this.context)
        Log.i(TAG, "🚀 SDK Initialized for $appName via ${owner::class.java.simpleName}")
    }

    /**
     * Starts the data collection and measurement process.
     */
    fun startNWMeasurement(
        msisdn: String,
        integratedAppVersion: String,
        sdkInitiateTimeStamp: String,
        integratedAppEventName: String,
        userLatitude: Double = 0.0,
        userLongitude: Double = 0.0,
        callback: (Boolean, FWAMeasurementStatus) -> Unit
    ) {
        if (!isInitialized()) {
            handleUninitializedError(callback)
            return
        }

        try {
            requestPermission { isGranted ->
                val validator = SdkContainer.preFlightValidator
                val validationError = validator?.validate()

                if (!isGranted || validationError != null) {
                    handlePermissionDenied(callback)
                } else {
                    performMeasurement(
                        msisdn,
                        integratedAppVersion,
                        sdkInitiateTimeStamp,
                        integratedAppEventName,
                        userLatitude,
                        userLongitude,
                        callback
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting measurement process: ${e.message}")
            dispatchErrorCallback(callback, "Error starting measurement process")
        }
    }

    private fun isInitialized(): Boolean {
        return this::checkPermissionHandler.isInitialized && 
               this::callbackDispatcher.isInitialized && 
               SdkContainer.isInitialized()
    }

    private fun handleUninitializedError(callback: (Boolean, FWAMeasurementStatus) -> Unit) {
        Log.e(TAG, "SDK not initialized. Call init() first.")
        val errorResponse = NetworkDataResponse(
            status = Constants.STATUS_FAILED,
            statusCode = 400,
            message = "SDK not initialized"
        )
        callback(false, createMeasurementStatus(errorResponse, isSdkInit = false))
    }

    private fun handlePermissionDenied(callback: (Boolean, FWAMeasurementStatus) -> Unit) {
        val validator = SdkContainer.preFlightValidator
        val validationError = validator?.validate()

        val errorMessage = validationError?.first ?: "Required permissions or GPS are missing."
        val statusCode = validationError?.second ?: 400

        val error = NetworkDataResponse(
            status = Constants.STATUS_FAILED,
            testResult = Constants.RESULT_FAILED,
            statusCode = statusCode,
            message = errorMessage
        )
        callbackDispatcher.dispatch(callback, false, createMeasurementStatus(error))
    }

    private fun performMeasurement(
        msisdn: String,
        integratedAppVersion: String,
        sdkInitiateTimeStamp: String,
        integratedAppEventName: String,
        userLatitude: Double,
        userLongitude: Double,
        callback: (Boolean, FWAMeasurementStatus) -> Unit
    ) {
        SdkContainer.coroutineScope?.launch {
            val input = FWAAssessmentExecutionInput(
                msisdn,
                integratedAppVersion,
                sdkInitiateTimeStamp,
                integratedAppEventName,
                userLatitude,
                userLongitude
            )

            val response = withTimeoutOrNull(EXECUTION_TIMEOUT_MS) {
                SdkContainer.networkDataCaptureExecutor?.execute(input)
            }

            if (response != null) {
                val isSuccess = response.status.equals(Constants.STATUS_SUCCESS, ignoreCase = true)
                callbackDispatcher.dispatch(callback, isSuccess, createMeasurementStatus(response))
            } else {
                dispatchErrorCallback(callback, Constants.Assessment_Timeout_Message)
            }
        } ?: run {
            dispatchErrorCallback(callback, "SDK initialization failed.")
        }
    }

    private fun dispatchErrorCallback(
        callback: (Boolean, FWAMeasurementStatus) -> Unit, message: String
    ) {
        val errorResponse = NetworkDataResponse(
            status = Constants.STATUS_FAILED,
            statusCode = 400,
            message = message
        )
        callbackDispatcher.dispatch(callback, false, createMeasurementStatus(errorResponse))
    }

    private fun createMeasurementStatus(
        networkDataResponse: NetworkDataResponse,
        isSdkInit: Boolean = this::checkPermissionHandler.isInitialized
    ): FWAMeasurementStatus {
        val nsp = SdkContainer.networkStateProvider
        return FWAMeasurementStatus(
            isSdkInit = isSdkInit,
            isLocationEnabled = nsp?.let { it.hasLocationPermission() && it.isGpsEnabled() } ?: false,
            isPhoneStateGranted = nsp?.hasPhoneStatePermission() ?: false,
            response = gson.toJson(networkDataResponse)
        )
    }

    private fun requestPermission(callback: (Boolean) -> Unit) {
        if (this::checkPermissionHandler.isInitialized) {
            if (checkPermissionHandler.isPermissionGranted()) {
                callback(true)
            } else {
                checkPermissionHandler.requestPermission(callback = callback)
            }
        } else {
            callback(false)
        }
    }
}
