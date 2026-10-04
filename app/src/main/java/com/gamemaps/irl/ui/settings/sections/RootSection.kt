package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.SettingsSection
import com.gamemaps.irl.ui.settings.SettingsUiState
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow

/** Menu racine : un groupe par domaine, chaque ligne ouvre un sous-menu. */
@Composable
fun RootSection(state: SettingsUiState, open: (SettingsSection) -> Unit) {
    val settings = state.settings
    val saved = state.savedPlaces

    SettingsGroup("Carte") {
        SettingsNavigationRow(HudIcons.Palette, "Thème", settings.theme.label) { open(SettingsSection.THEME) }
        SettingsNavigationRow(HudIcons.Videocam, "Perspective", settings.perspective.label) { open(SettingsSection.PERSPECTIVE) }
        SettingsNavigationRow(HudIcons.Car, "Véhicule", settings.vehicle.label) { open(SettingsSection.VEHICLE) }
    }

    SettingsGroup("Navigation") {
        SettingsNavigationRow(HudIcons.VolumeOn, "Audio", if (state.isMuted) "Son coupé" else "Son actif") { open(SettingsSection.AUDIO) }
        SettingsNavigationRow(HudIcons.Radar, "Radars", if (settings.radarAlerts) "Alertes actives" else "Alertes désactivées") { open(SettingsSection.RADARS) }
        SettingsNavigationRow(HudIcons.Traffic, "Trafic", trafficLabel(settings.traffic, hasKey = state.maskedTomTomKey != null)) { open(SettingsSection.TRAFFIC) }
    }

    SettingsGroup("Lieux") {
        val placesCount = listOfNotNull(saved.home, saved.work).size + saved.favorites.size
        SettingsNavigationRow(Icons.Filled.Star, "Lieux enregistrés", "$placesCount") { open(SettingsSection.PLACES) }
    }

    SettingsGroup("Application") {
        SettingsNavigationRow(Icons.Filled.Info, "À propos", null) { open(SettingsSection.ABOUT) }
    }
}

/** Résumé affiché sur la ligne "Trafic" du menu racine. */
private fun trafficLabel(enabled: Boolean, hasKey: Boolean): String = when {
    !hasKey -> "Clé manquante"
    enabled -> "Temps réel"
    else -> "Désactivé"
}
