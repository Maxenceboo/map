package com.gamemaps.irl.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.navigation.NavigationState

/**
 * Pousse l'état vers la carte à chaque changement :
 * position, itinéraire (+ épingle), radars, et cadrage de la caméra selon la phase du trajet.
 */
@Composable
fun MapRenderEffect(controller: MapController?, state: MainUiState) {
    val navigation = state.navigation
    val preview = navigation as? NavigationState.Previewing
    val route = (navigation as? NavigationState.Navigating)?.route ?: preview?.route
    val fix = state.driving.fix
    val radars = state.driving.radars
    val isNavigating = navigation is NavigationState.Navigating

    LaunchedEffect(controller, fix) {
        if (fix != null) controller?.showVehicle(fix)
    }
    LaunchedEffect(controller, route) {
        controller?.showRoute(route)
    }
    LaunchedEffect(controller, radars) {
        controller?.showRadars(radars)
    }
    // Aperçu : on cadre tout le trajet. Départ (ou annulation) : retour derrière le véhicule.
    LaunchedEffect(controller, preview?.route) {
        preview?.route?.let { controller?.showOverview(it) }
    }
    LaunchedEffect(controller, isNavigating, preview == null) {
        if (preview == null) controller?.recenter()
    }
}
