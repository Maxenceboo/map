package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.routing.tomtom.TomTomApiKey
import com.gamemaps.irl.ui.settings.SettingsSection
import com.gamemaps.irl.ui.settings.SettingsUiState
import com.gamemaps.irl.ui.settings.components.SettingsGroupTitle
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow

/** Menu racine : un groupe par domaine, chaque ligne ouvre un sous-menu. */
@Composable
fun RootSection(state: SettingsUiState, open: (SettingsSection) -> Unit) {
    val settings = state.settings
    val saved = state.savedPlaces

    SettingsGroupTitle("Carte")
    SettingsNavigationRow("🎨", "Thème", settings.theme.label) { open(SettingsSection.THEME) }
    SettingsNavigationRow("🎥", "Perspective", settings.perspective.label) { open(SettingsSection.PERSPECTIVE) }
    SettingsNavigationRow("🚗", "Véhicule", settings.vehicle.label) { open(SettingsSection.VEHICLE) }

    SettingsGroupTitle("Navigation")
    SettingsNavigationRow("🔊", "Audio", if (state.isMuted) "Son coupé" else "Son actif") { open(SettingsSection.AUDIO) }
    SettingsNavigationRow("📸", "Radars", if (settings.radarAlerts) "Alertes actives" else "Alertes désactivées") { open(SettingsSection.RADARS) }
    SettingsNavigationRow("🚦", "Trafic", trafficLabel(settings.traffic)) { open(SettingsSection.TRAFFIC) }

    SettingsGroupTitle("Lieux")
    val placesCount = listOfNotNull(saved.home, saved.work).size + saved.favorites.size
    SettingsNavigationRow("⭐", "Lieux enregistrés", "$placesCount") { open(SettingsSection.PLACES) }

    SettingsGroupTitle("Application")
    SettingsNavigationRow("📱", "À propos", null) { open(SettingsSection.ABOUT) }
}

/** Résumé affiché sur la ligne "Trafic" du menu racine. */
private fun trafficLabel(enabled: Boolean): String = when {
    !TomTomApiKey.isConfigured -> "Clé manquante"
    enabled -> "Temps réel"
    else -> "Désactivé"
}
