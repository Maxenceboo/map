package com.gamemaps.irl.ui.settings

import com.gamemaps.irl.data.custom.CustomThemeSpec
import com.gamemaps.irl.data.custom.CustomVehicleSpec
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.vehicle3d.VehicleKind

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
    /** Thèmes et véhicules créés en mode développeur. */
    val customThemes: List<CustomThemeSpec> = emptyList(),
    val customVehicles: List<CustomVehicleSpec> = emptyList(),
) {
    /** Thèmes proposés dans Paramètres > Thème : ceux de l'app, puis ceux de l'utilisateur. */
    val allThemes: List<MapTheme> get() = MapTheme.entries + customThemes.map { it.toMapTheme() }

    /** Véhicules proposés dans Paramètres > Véhicule > Modèle. */
    val allVehicles: List<VehicleKind> get() = VehicleKind.entries + customVehicles.map { it.toVehicleKind() }
}
