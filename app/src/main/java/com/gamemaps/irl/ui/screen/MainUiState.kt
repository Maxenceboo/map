package com.gamemaps.irl.ui.screen

import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.ui.search.SearchUiState

/** Tout ce que l'écran principal affiche, en un seul objet. */
data class MainUiState(
    val search: SearchUiState = SearchUiState(),
    val navigation: NavigationState = NavigationState.Idle,
    val driving: DrivingState = DrivingState(),
    val isMuted: Boolean = false,
    val savedPlaces: SavedPlaces = SavedPlaces(),
)
