package com.ptsl.selective_network_sdk.data_model.entity

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Optimized data model for network telemetry.
 * Stateless version: No Room annotations.
 */
@Parcelize
@Keep
data class NetworkDataEntity(
    @SerializedName("msisdn") var msisdn: String = "",
    @SerializedName("sdkInitiateTimeStamp") var sdkInitiateTimeStamp: String = "",
    @SerializedName("integratedAppVersion") var integratedAppVersion: String = "",
    @SerializedName("integratedAppEventName") var integratedAppEventName: String = "Event",
    @SerializedName("userLatitude") var userLatitude: Double = 0.0,
    @SerializedName("userLongitude") var userLongitude: Double = 0.0,
    @SerializedName("time") var time: String = "00:00:00",
    @SerializedName("date") var date: String = "00-00-2000",
    @SerializedName("mcc") var mcc: String = "000",
    @SerializedName("mnc") var mnc: String = "00",
    @SerializedName("lac") var lac: String? = null,
    @SerializedName("tac") var tac: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("cid") var cid: String? = null,
    @SerializedName("enb") var enb: String? = null,
    @SerializedName("psc") var psc: String? = null,
    @SerializedName("pci") var pci: String? = null,
    @SerializedName("ta") var ta: String? = null,
    @SerializedName("bw") var bw: String? = null,
    @SerializedName("arfcn") var arfcn: String? = null,
    @SerializedName("band") var band: String? = null,
    @SerializedName("rxlev") var rxlev: String? = null,
    @SerializedName("rxQual") var rxQual: String? = null,
    @SerializedName("rscp") var rscp: String? = null,
    @SerializedName("ecNo") var ecNo: String? = null,
    @SerializedName("rsrp") var rsrp: String? = null,
    @SerializedName("rsrq") var rsrq: String? = null,
    @SerializedName("snr") var snr: String? = null,
    @SerializedName("cqi") var cqi: String? = null,
    @SerializedName("rssi") var rssi: String? = null,
    @SerializedName("longitude") var longitude: Double? = null,
    @SerializedName("latitude") var lattitude: Double? = null,
    @SerializedName("ulspeed") var ulspeed: Double? = null,
    @SerializedName("dlspeed") var dlspeed: Double? = null,
    @SerializedName("data") var data: String? = null,
    @SerializedName("bitRateError") var bitRateError: String? = null,
    @SerializedName("inDoor") var inDoor: Boolean = false,
    @SerializedName("outDoor") var outDoor: Boolean = false,
    @SerializedName("deviceModel") var deviceModel: String? = null,
    @SerializedName("superOfficeTicket") var superOfficeTicket: Int? = null,
    @SerializedName("isDataCaptureOffline") var isDataCaptureOffline: Boolean = false,
    @SerializedName("isUserDeviceOnCall") var isUserDeviceOnCall: Boolean = false,
    @SerializedName("deviceManufacture") var deviceManufacture: String? = null,
    @SerializedName("deviceOsVersion") var deviceOsVersion: String? = null,
    @SerializedName("usedSimSlot") var usedSimSlot: Int? = null,
    @SerializedName("rtt") var rtt: Double? = null,
    @SerializedName("latency") var latency: Double? = null,
    @SerializedName("totalDownloadVolume") var totalDownloadVolume: Double? = null,
    @SerializedName("totalUploadVolume") var totalUploadVolume: Double? = null
) : Parcelable