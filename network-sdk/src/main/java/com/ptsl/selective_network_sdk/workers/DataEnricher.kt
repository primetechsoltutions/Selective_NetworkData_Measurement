package com.ptsl.selective_network_sdk.workers

import com.ptsl.selective_network_sdk.data_model.entity.NetworkDataEntity

class DataEnricher {
    /**
     * Enriches the raw captured data with input parameters and external metadata.
     */
    fun enrich(
        rawDataList: List<NetworkDataEntity>,
        msisdn: String,
        integratedAppVersion: String,
        sdkInitiateTimeStamp: String,
        integratedAppEventName: String,
        userLatitude: Double,
        userLongitude: Double
    ): List<NetworkDataEntity> {
        return rawDataList.map { data ->
            data.apply {
                this.msisdn = msisdn
                this.integratedAppVersion = integratedAppVersion
                this.sdkInitiateTimeStamp = sdkInitiateTimeStamp
                this.integratedAppEventName = integratedAppEventName
                this.userLatitude = userLatitude
                this.userLongitude = userLongitude
            }
        }
    }
}
