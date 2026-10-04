package com.gamemaps.irl.service

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.gamemaps.irl.data.location.LocationPermissions
import com.gamemaps.irl.navigation.NavigationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Démarre le service de guidage au début d'un trajet et l'arrête à la fin.
 * "Trajet" = calcul, aperçu ou guidage en cours ; tout le reste (arrivée, échec, arrêt) l'arrête.
 */
class NavigationServiceController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val navigation: StateFlow<NavigationState>,
) {
    fun start() {
        scope.launch {
            navigation.map(::needsService).distinctUntilChanged().collect { needed ->
                if (needed) startService() else stopService()
            }
        }
    }

    private fun needsService(state: NavigationState): Boolean =
        state is NavigationState.Navigating || state is NavigationState.Calculating || state is NavigationState.Previewing

    private fun startService() {
        // Un service "localisation" exige la permission de localisation.
        if (!LocationPermissions.isGranted(context)) return
        try {
            ContextCompat.startForegroundService(context, NavigationForegroundService.startIntent(context))
        } catch (e: IllegalStateException) {
            // Android 12+ interdit de démarrer un tel service depuis l'arrière-plan (cas rare :
            // trajet lancé alors qu'aucun écran de l'app n'est visible). Le guidage continue au premier plan.
            Log.w(TAG, "Service de guidage non démarré : ${e.message}")
        }
    }

    private fun stopService() {
        context.stopService(NavigationForegroundService.startIntent(context))
    }

    private companion object {
        const val TAG = "NavigationService"
    }
}
