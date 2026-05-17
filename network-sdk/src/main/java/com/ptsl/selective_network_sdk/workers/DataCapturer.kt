package com.ptsl.selective_network_sdk.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.ptsl.selective_network_sdk.data_model.NetworkMetrics
import com.ptsl.selective_network_sdk.data_model.entity.NetworkDataEntity
import com.ptsl.selective_network_sdk.provider.NetworkStateProvider
import com.ptsl.selective_network_sdk.provider.SimInfoProvider
import com.ptsl.selective_network_sdk.utils.DateTimeProvider
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.model.cell.CellGsm
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellWcdma
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import kotlin.math.roundToInt

/**
 * Clean DataCapturer: Orchestrates data collection from system services and mappers.
 * SOLID: Open/Closed - uses providers to abstract system details.
 */
class DataCapturer(
    private val context: Context,
    private val networkStateProvider: NetworkStateProvider,
    private val simInfoProvider: SimInfoProvider,
    private val mapper: CellDataMapper,
    private val timeProvider: DateTimeProvider
) {
    suspend fun captureData(
        metrics: NetworkMetrics
    ): List<NetworkDataEntity> {
        val dataList = arrayListOf<NetworkDataEntity>()
        val isMobileConnected = networkStateProvider.isMobileConnected()
        val activeNetworkMnc = if (isMobileConnected) simInfoProvider.getActiveNetworkMNC() else "-1"
        val simCount = simInfoProvider.getSimCount()
        val isUserOnCall = simInfoProvider.isUserOnCall()
        val locationPair = LocationHelper.getCurrentLocation(context)


        val cells = try {
            if (networkStateProvider.hasRequiredPermissions()) {
                NetMonsterFactory.get(context).getCells()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        if (cells.isNullOrEmpty()) {
            dataList.add(createFallbackEntity(locationPair, isMobileConnected, simCount, metrics))
            return dataList
        }

        cells.forEach { cell ->
            if (cell.connectionStatus is PrimaryConnection) {
                dataList.add(
                    mapper.mapToEntity(
                        cell,
                        locationPair,
                        isMobileConnected,
                        activeNetworkMnc,
                        simCount,
                        rtt = (metrics.rtt * 100).roundToInt() / 100.0,
                        latency = (metrics.latency * 100).roundToInt() / 100.0,
                        isUserOnCall,
                        timeProvider
                    )
                )
            }
        }
        return dataList
    }

    fun getNetworkIdentifiers(): Map<String, Any>? {
        if (!networkStateProvider.hasRequiredPermissions()) return null

        val cells = try {
            NetMonsterFactory.get(context).getCells()
        } catch (e: Exception) {
            null
        }

        // Filter for primary connection and Banglalink SIM (MNC "03" or "3" based on SimInfoProvider)
        val primaryCell = cells?.find { cell ->
            cell.connectionStatus is PrimaryConnection &&
                    cell.network?.mnc?.removePrefix("0") == "3"
        } ?: return null

        val cid = when (primaryCell) {
            is CellGsm -> primaryCell.cid
            is CellWcdma -> primaryCell.cid
            is CellLte -> primaryCell.cid
            else -> null
        }

        val lacid = when (primaryCell) {
            is CellGsm -> primaryCell.lac
            is CellWcdma -> primaryCell.lac
            is CellLte -> primaryCell.enb
            else -> null
        }

        val type = when (primaryCell) {
            is CellGsm -> "GSM"
            is CellWcdma -> "WCDMA"
            is CellLte -> "LTE"
            else -> null
        }

        if (cid == null || lacid == null || type == null) return null

        return mapOf(
            "CID" to cid,
            "LACID" to lacid,
            "Technology" to type
        )
    }

    private fun createFallbackEntity(
        location: Pair<Double, Double>,
        isMobile: Boolean,
        simCount: Int,
        metrics: NetworkMetrics
    ): NetworkDataEntity {
        return NetworkDataEntity(
            lattitude = location.first,
            longitude = location.second,
            data = if (isMobile) "Mobile" else "Wifi",
            usedSimSlot = simCount,
            rtt = (metrics.rtt * 100).roundToInt() / 100.0,
            latency = (metrics.latency * 100).roundToInt() / 100.0,
            time = timeProvider.getCurrentDateTime(),
            date = timeProvider.getCurrentDate(),
            deviceModel = Build.MODEL,
            deviceManufacture = Build.MANUFACTURER,
            deviceOsVersion = Build.VERSION.SDK_INT.toString()
        )
    }
}
