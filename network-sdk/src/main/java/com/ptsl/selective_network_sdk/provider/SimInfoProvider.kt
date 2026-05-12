package com.ptsl.selective_network_sdk.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

interface SimInfoProvider {
    fun getActiveNetworkMNC(): String
    fun getSimCount(): Int
    fun isBanglalinkDataEnabled(): Boolean
    fun hasBanglalinkSimPresent(): Boolean
    fun isUserOnCall(): Boolean
}

class SimInfoProviderImpl(private val context: Context) : SimInfoProvider {
    
    override fun getActiveNetworkMNC(): String {
        var mnc = "-1"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val sm = SubscriptionManager.from(context)
                val dataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
                if (dataSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    val si = sm.getActiveSubscriptionInfo(dataSubId)
                    mnc = si?.mnc?.toString() ?: "-1"
                }
            }
        } catch (_: Exception) { }
        return "0${mnc}"
    }

    override fun getSimCount(): Int {
        return try {
            val sm = SubscriptionManager.from(context)
            sm.activeSubscriptionInfoList?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    override fun isBanglalinkDataEnabled(): Boolean {
        return try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            val sm = SubscriptionManager.from(context)
            val defaultDataId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SubscriptionManager.getDefaultDataSubscriptionId()
            } else {
                -1
            }

            if (defaultDataId != -1) {
                val info = sm.getActiveSubscriptionInfo(defaultDataId)
                info?.mnc?.toString()?.removePrefix("0") == "3"
            } else {
                sm.activeSubscriptionInfoList?.firstOrNull()?.mnc?.toString()?.removePrefix("0") == "3"
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun hasBanglalinkSimPresent(): Boolean {
        return try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            val sm = SubscriptionManager.from(context)
            sm.activeSubscriptionInfoList?.any { it.mnc.toString().removePrefix("0") == "3" } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun isUserOnCall(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.callState == TelephonyManager.CALL_STATE_RINGING ||
                    telephonyManager.callState == TelephonyManager.CALL_STATE_OFFHOOK
        } catch (_: Exception) {
            false
        }
    }
}
