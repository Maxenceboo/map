package com.gamemaps.irl.ui.settings

import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.settings.AppSettings

/** Ce qu'affiche le menu Paramètres. */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val savedPlaces: SavedPlaces = SavedPlaces(),
    val isMuted: Boolean = false,
)
