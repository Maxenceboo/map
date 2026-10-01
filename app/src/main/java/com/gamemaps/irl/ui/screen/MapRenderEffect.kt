package com.gamemaps.irl.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.navigation.NavigationState

/** Pousse la position et l'itinéraire vers la carte à chaque changement. */
@Composable
fun MapRenderEffect(controller: MapController?, state: MainUiState) {
    val route = (state.navigation as? NavigationState.Navigating)?.route

    LaunchedEffect(controller, state.fix) {
        val fix = state.fix ?: return@LaunchedEffect
        controller?.showVehicle(fix)
    }
    LaunchedEffect(controller, route) {
        controller?.showRoute(route)
    }
}
