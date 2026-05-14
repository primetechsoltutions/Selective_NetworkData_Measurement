package com.ptsl.selective_network_sdk.workers

import android.os.Build
import com.ptsl.selective_network_sdk.data_model.entity.NetworkDataEntity
import com.ptsl.selective_network_sdk.dl_ul_test.DownloadUploadHelper
import com.ptsl.selective_network_sdk.utils.DateTimeProvider
import cz.mroczis.netmonster.core.model.cell.*

/**
 * Dedicated Mapper for transforming NetMonster cell data into the internal data model.
 * SOLID: Single Responsibility - handles only data mapping.
 */
class CellDataMapper(private val downloader: DownloadUploadHelper) {

    suspend fun mapToEntity(
        cell: ICell,
        locationPair: Pair<Double, Double>,
        isMobileConnected: Boolean,
        activeNetworkMnc: String,
        simCount: Int,
        rtt: Double,
        latency: Double,
        isUserOnCall: Boolean,
        timeProvider: DateTimeProvider
    ): NetworkDataEntity {
        val mcc = cell.network?.mcc
        val mnc = cell.network?.mnc
        
        val networkType = getNetworkType(cell)

        val speedResult = if (isMobileConnected) {
            val retries = if (networkType == "4G") 2 else 1
            downloader.getBandWidthSpeed(
                networkType = networkType,
                retryCountDownload = retries,
                retryCountUpload = retries,
                hasMobileInternet = isMobileConnected,
                currentMnc = mnc,
                activeNetworkMnc = activeNetworkMnc
            )
        } else null

        return NetworkDataEntity().apply {
            time = timeProvider.getCurrentDateTime()
            date = timeProvider.getCurrentDate()
            this.mcc = mcc ?: "000"
            this.mnc = mnc ?: "00"
            type = if (cell is CellNr) "5G" else if (cell is CellLte) "4G" else networkType
            lattitude = locationPair.first
            longitude = locationPair.second
            deviceModel = Build.MODEL
            deviceManufacture = Build.MANUFACTURER
            deviceOsVersion = Build.VERSION.SDK_INT.toString()
            data = if (isMobileConnected) "Mobile" else "Wifi"
            this.isUserDeviceOnCall = isUserOnCall
            usedSimSlot = simCount
            this.rtt = rtt
            this.latency = latency
            
            speedResult?.let { speed ->
                dlspeed = speed.downloadSpeedKbps
                ulspeed = speed.uploadSpeedKbps
                totalDownloadVolume = speed.totalDownloadMB
                totalUploadVolume = speed.totalUploadMB
            }

            mapCellSpecifics(this, cell)
        }
    }

    private fun getNetworkType(cell: ICell): String {
        return when (cell) {
            is CellGsm, is CellCdma -> "2G"
            is CellWcdma, is CellTdscdma -> "3G"
            is CellLte -> "4G"
            is CellNr -> "5G"
            else -> "Unknown"
        }
    }

    private fun mapCellSpecifics(entity: NetworkDataEntity, cell: ICell) {
        when (cell) {
            is CellGsm -> {
                entity.lac = cell.lac?.toString()
                entity.cid = cell.cid?.toString()
                entity.arfcn = cell.band?.arfcn?.toString()
                entity.rssi = cell.signal.rssi?.toString()
                entity.band = cell.band?.name
            }
            is CellWcdma -> {
                entity.lac = cell.lac?.toString()
                entity.cid = cell.cid?.toString()
                entity.psc = cell.psc?.toString()
                entity.rscp = cell.signal.rscp?.toString()
                entity.rssi = cell.signal.rssi?.toString()
                entity.band = cell.band?.name
            }
            is CellLte -> {
                entity.tac = cell.tac?.toString()
                entity.cid = cell.cid?.toString()
                entity.enb = cell.enb?.toString()
                entity.pci = cell.pci?.toString()
                entity.rsrp = cell.signal.rsrp?.toString()
                entity.rsrq = cell.signal.rsrq?.toString()
                entity.snr = cell.signal.snr?.toString()
                entity.rssi = cell.signal.rssi?.toString()
                entity.band = cell.band?.name
                entity.bw = cell.bandwidth?.toString()
            }
            is CellNr -> {
                entity.tac = cell.tac?.toString()
                entity.pci = cell.pci?.toString()
                entity.rsrp = cell.signal.ssRsrp?.toString()
                entity.rsrq = cell.signal.ssRsrq?.toString()
                entity.snr = cell.signal.ssSinr?.toString()
                entity.band = cell.band?.name
            }
        }
    }
}
