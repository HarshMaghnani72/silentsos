package com.silentsos.app.utils

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides real-time system status information (GPS, battery, network)
 * to replace hardcoded dashboard values.
 */
@Singleton
class SystemStatusProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Returns "Active" if GPS is enabled, "Disabled" otherwise. */
    fun getGpsStatus(): String {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
            "Active"
        } else {
            "Disabled"
        }
    }

    /** Returns the current battery level as a percentage (0–100). */
    fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
    }

    /** Returns the current network type label (WiFi, 5G, LTE, 3G, None). */
    fun getNetworkStatus(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "None"
        val network = connectivityManager.activeNetwork ?: return "None"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "None"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Attempt to identify cellular generation
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) "WiFi"
                else "LTE"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
    }
}
