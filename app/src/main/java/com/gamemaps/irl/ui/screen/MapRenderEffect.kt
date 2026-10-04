package com.gamemaps.irl.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.navigation.NavigationState

/** Pousse la position, l'itinéraire et les radars vers la carte à chaque changement. */
@Composable
fun MapRenderEffect(controller: MapController?, state: MainUiState) {
    val route = (state.navigation as? NavigationState.Navigating)?.route
    val fix = state.driving.fix
    val radars = state.driving.radars

    LaunchedEffect(controller, fix) {
        if (fix != null) controller?.showVehicle(fix)
    }
    LaunchedEffect(controller, route) {
        controller?.showRoute(route)
    }
    LaunchedEffect(controller, radars) {
        controller?.showRadars(radars)
    }
}
