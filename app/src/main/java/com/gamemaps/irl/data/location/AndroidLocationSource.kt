package com.gamemaps.irl.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Positions issues du [LocationManager] Android (pas de dépendance aux Google Play Services).
 * Utilise le fournisseur "fused" quand il existe (Android 12+), sinon le GPS pur.
 */
class AndroidLocationSource(private val context: Context) : LocationSource {

    @SuppressLint("MissingPermission") // Vérifiée au début du flow via LocationPermissions.
    override fun fixes(): Flow<GpsFix> = callbackFlow {
        if (!LocationPermissions.isGranted(context)) {
            close(SecurityException("Permission de localisation manquante"))
            return@callbackFlow
        }
        val manager = context.getSystemService(LocationManager::class.java)
        val provider = bestProvider(manager)

        // Toutes les méthodes sont redéfinies : avant Android 11 elles étaient abstraites.
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location.toGpsFix())
            }

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit

            @Deprecated("Requis avant Android 10")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        manager.getLastKnownLocation(provider)?.let { trySend(it.toGpsFix()) }
        manager.requestLocationUpdates(provider, UPDATE_INTERVAL_MS, 0f, listener, Looper.getMainLooper())
        awaitClose { manager.removeUpdates(listener) }
    }

    private fun bestProvider(manager: LocationManager): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && manager.hasProvider(LocationManager.FUSED_PROVIDER)) {
            LocationManager.FUSED_PROVIDER
        } else {
            LocationManager.GPS_PROVIDER
        }

    private companion object {
        const val UPDATE_INTERVAL_MS = 500L
    }
}
