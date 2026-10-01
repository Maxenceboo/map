package com.gamemaps.irl.ui.screen

import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.GpsQuality
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.ui.search.SearchUiState

/** Tout ce que l'écran principal affiche, en un seul objet. */
data class MainUiState(
    val search: SearchUiState = SearchUiState(),
    val navigation: NavigationState = NavigationState.Idle,
    val fix: GpsFix? = null,
) {
    val gpsQuality: GpsQuality get() = GpsQuality.from(fix)
}
