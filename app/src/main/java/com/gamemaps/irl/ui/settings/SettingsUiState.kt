package com.gamemaps.irl.ui.settings

import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.settings.AppSettings

/** Ce qu'affiche le menu Paramètres. */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val savedPlaces: SavedPlaces = SavedPlaces(),
    val isMuted: Boolean = false,
    /** Clé TomTom enregistrée, masquée pour l'affichage ; null s'il n'y en a pas. */
    val maskedTomTomKey: String? = null,
    /** Résultats de la recherche d'adresse pour définir Maison ou Travail. */
    val placeResults: List<Place> = emptyList(),
    /** false tant que le GPS n'a pas de position : "Ma position actuelle" est alors indisponible. */
    val hasPosition: Boolean = false,
)
