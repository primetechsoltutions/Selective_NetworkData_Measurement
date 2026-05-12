package com.ptsl.selective_network_sdk.data_model
data class FWAMeasurementStatus(
    val isSdkInit: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isPhoneStateGranted: Boolean = false,
    val sdkVersion: String = com.ptsl.selective_network_sdk.BuildConfig.SdkVersion,
    val response: String? = null
)
