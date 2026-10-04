package com.gamemaps.irl.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Permissions de localisation nécessaires, partagées entre le téléphone et Android Auto. */
object LocationPermissions {

    val REQUIRED = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    /**
     * Ce que demande l'écran du téléphone au lancement : la localisation, plus (Android 13+)
     * les notifications, pour afficher la notification de guidage écran éteint.
     */
    val PHONE_REQUEST: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) REQUIRED + Manifest.permission.POST_NOTIFICATIONS else REQUIRED

    fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
