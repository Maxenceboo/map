package com.gamemaps.irl.ui.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.gamemaps.irl.data.location.LocationPermissions

/**
 * Demande la permission de localisation au premier affichage,
 * puis appelle [onGranted] dès qu'elle est accordée (ou si elle l'était déjà).
 */
@Composable
fun LocationPermissionEffect(onGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (LocationPermissions.isGranted(context)) onGranted()
    }
    LaunchedEffect(Unit) {
        if (LocationPermissions.isGranted(context)) onGranted() else launcher.launch(LocationPermissions.REQUIRED)
    }
}
