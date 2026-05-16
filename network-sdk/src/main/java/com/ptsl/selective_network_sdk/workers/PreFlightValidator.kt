package com.ptsl.selective_network_sdk.workers

import com.ptsl.selective_network_sdk.provider.NetworkStateProvider
import com.ptsl.selective_network_sdk.provider.SimInfoProvider
import com.ptsl.selective_network_sdk.utils.Constants
import android.content.Context

/**
 * Clean PreFlightValidator: Validates environment state before execution.
 * SOLID: Single Responsibility - focuses on validation logic only.
 */
class PreFlightValidator(
    private val context: Context,
    private val networkStateProvider: NetworkStateProvider,
    private val simInfoProvider: SimInfoProvider
) {
    private val rules: List<ValidationRule> = listOf(
        PermissionRule(networkStateProvider),
        GpsRule(networkStateProvider),
        WifiRule(networkStateProvider),
        SimPresenceRule(simInfoProvider),
        MobileDataRule(networkStateProvider),
        BanglalinkDataRule(simInfoProvider)
    )

    fun validate(
        skipWifiCheck: Boolean = false,
        skipMobileDataCheck: Boolean = false
    ): Pair<String, Int>? {
        for (rule in rules) {
            if (skipWifiCheck && rule is WifiRule) continue
            if (skipMobileDataCheck && (rule is MobileDataRule || rule is BanglalinkDataRule)) continue
            val result = rule.validate()
            if (result != null) return result
        }
        return null
    }

    // ─── Rule Implementations ────────────────────────────────────────────────

    private inner class PermissionRule(private val provider: NetworkStateProvider) : ValidationRule {
        override fun validate(): Pair<String, Int>? {
            if (!provider.hasPhoneStatePermission()) {
                return Pair(Constants.ERR_MSG_PHONE_STATE_PERMISSION_DENIED, 400)
            }
            if (!provider.hasLocationPermission()) {
                return Pair(Constants.ERR_MSG_PERMISSION_DENIED, 400)
            }
            return null
        }
    }

    private inner class GpsRule(private val provider: NetworkStateProvider) : ValidationRule {
        override fun validate(): Pair<String, Int>? {
            if (!provider.isGpsEnabled()) {
                return Pair(Constants.ERR_MSG_GPS_DISABLED, 400)
            }
            return null
        }
    }

    private inner class WifiRule(private val provider: NetworkStateProvider) : ValidationRule {
        override fun validate(): Pair<String, Int>? {
            if (provider.isWifiConnected()) {
                return Pair(Constants.ERR_MSG_WIFI_NOT_ALLOWED, 400)
            }
            return null
        }
    }

    private inner class SimPresenceRule(private val provider: SimInfoProvider) : ValidationRule {
        override fun validate(): Pair<String, Int>? {
            if (!provider.hasBanglalinkSimPresent()) {
                return Pair(Constants.ERR_MSG_BL_SIM_REQUIRED, 400)
            }
            return null
        }
    }

    private inner class MobileDataRule(private val provider: NetworkStateProvider) : ValidationRule {
        override fun validate(): Pair<String, Int>? {
            if (!provider.isMobileConnected()) {
                return Pair(Constants.ERR_MSG_MOBILE_DATA_REQUIRED, 400)
            }
            return null
        }
    }

    private inner class BanglalinkDataRule(private val provider: SimInfoProvider) : ValidationRule {
        override fun validate(): Pair<String, Int>? {
            if (!provider.isBanglalinkDataEnabled()) {
                return Pair(Constants.ERR_MSG_BL_DATA_REQUIRED, 400)
            }
            return null
        }
    }
}
