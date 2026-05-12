package com.ptsl.selective_network_sdk.data_model
data class FWAAssessmentExecutionInput(
    val msisdn: String,
    val integratedAppVersion: String,
    val sdkInitiateTimeStamp: String,
    val integratedAppEventName: String,
    val userLatitude: Double,
    val userLongitude: Double
)
