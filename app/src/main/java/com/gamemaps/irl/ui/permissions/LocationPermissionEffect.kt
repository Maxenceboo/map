package com.gamemaps.irl.ui.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.gamemaps.irl.data.location.LocationPermissions

/**
 * Au premier affichage, demande ce qui manque parmi la localisation et (Android 13+) les notifications,
 * puis appelle [onGranted] dès que la localisation est accordée (ou si elle l'était déjà).
 *
 * Les notifications sont demandées même si la localisation est déjà accordée : sinon, après une mise à jour,
 * la notification de guidage écran éteint ne s'afficherait jamais.
 */
@Composable
fun LocationPermissionEffect(onGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (LocationPermissions.isGranted(context)) onGranted()
    }
    LaunchedEffect(Unit) {
        if (LocationPermissions.isGranted(context)) onGranted()
        val missing = LocationPermissions.PHONE_REQUEST.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) launcher.launch(missing.toTypedArray())
    }
}
