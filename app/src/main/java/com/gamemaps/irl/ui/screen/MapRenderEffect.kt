package com.gamemaps.irl.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.map.camera.CameraConfig
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
    val isNavigating = navigation is NavigationState.Navigating

    // Paramètres : thème et perspective appliqués à chaud.
    LaunchedEffect(controller, state.settings.theme) {
        controller?.applyTheme(state.settings.theme)
    }
    LaunchedEffect(controller, state.settings.perspective) {
        controller?.applyCameraConfig(CameraConfig.PHONE.forPerspective(state.settings.perspective))
    }
    LaunchedEffect(controller, state.settings.vehicle, state.settings.vehicleColor, state.settings.headlights) {
        controller?.applyVehicle(state.settings.vehicle, state.settings.vehicleColor, state.settings.headlights)
    }
    LaunchedEffect(controller, fix) {
        if (fix != null) controller?.showVehicle(fix)
    }
    LaunchedEffect(controller, route) {
        controller?.showRoute(route)
    }
    // Aperçu : les trajets non choisis restent visibles en gris.
    LaunchedEffect(controller, preview?.alternatives, preview?.route) {
        controller?.showAlternatives(preview?.alternatives.orEmpty().filter { it !== preview?.route })
    }
    // Aperçu : on cadre tous les trajets proposés. Départ (ou annulation) : retour derrière le véhicule.
    LaunchedEffect(controller, preview?.alternatives) {
        preview?.alternatives?.let { controller?.showOverview(it) }
    }
    LaunchedEffect(controller, isNavigating, preview == null) {
        if (preview == null) controller?.recenter()
    }
}
