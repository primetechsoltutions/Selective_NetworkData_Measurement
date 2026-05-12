package com.ptsl.selective_network_sdk.data_model
data class FWAMeasurementStatus(
    val isSdkInit: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isPhoneStateGranted: Boolean = false,
    val response: String? = null
)
