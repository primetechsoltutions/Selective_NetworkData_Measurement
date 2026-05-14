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
        userLatitude: Double,
        userLongitude: Double,
        metrics: NetworkMetrics
    ): List<NetworkDataEntity> {
        val dataList = arrayListOf<NetworkDataEntity>()
        val locationPair = Pair(userLatitude, userLongitude)

        val isMobileConnected = networkStateProvider.isMobileConnected()
        val activeNetworkMnc = if (isMobileConnected) simInfoProvider.getActiveNetworkMNC() else "-1"
        val simCount = simInfoProvider.getSimCount()
        val isUserOnCall = simInfoProvider.isUserOnCall()

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
