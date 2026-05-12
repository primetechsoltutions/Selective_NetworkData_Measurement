package com.ptsl.selective_network_sdk.utils

object Constants {
    const val STATUS_SUCCESS = "Success"
    const val STATUS_FAILED = "Failed"
    const val RESULT_FAILED = "Failed"
    const val RESULT_SUCCESS = "Success"

    const val ERR_MSG_PERMISSION_DENIED = "Location permission are required to perform network measurement."
    const val ERR_MSG_PHONE_STATE_PERMISSION_DENIED = "Phone State permission is required to perform network measurement."
    const val ERR_MSG_GPS_DISABLED = "GPS is disabled. Please enable GPS to proceed."
    const val ERR_MSG_WIFI_NOT_ALLOWED = "Wi-Fi connection is not allowed. Please turn off Wi-Fi and enable Banglalink mobile data."
    const val ERR_MSG_BL_SIM_REQUIRED = "A Banglalink SIM card is required for this measurement."
    const val ERR_MSG_MOBILE_DATA_REQUIRED = "Mobile data connection is required. Please enable Banglalink mobile data."
    const val ERR_MSG_BL_DATA_REQUIRED = "Banglalink mobile data must be enabled and active for this measurement."
    const val Assessment_Timeout_Message = "Network assessment timed out. Please check your internet connection."
}
