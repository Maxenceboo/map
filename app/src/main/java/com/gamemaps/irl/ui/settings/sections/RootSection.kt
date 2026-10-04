package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
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

    SettingsGroupTitle("Navigation")
    SettingsNavigationRow("🔊", "Audio", if (state.isMuted) "Son coupé" else "Son actif") { open(SettingsSection.AUDIO) }
    SettingsNavigationRow("📸", "Radars", if (settings.radarAlerts) "Alertes actives" else "Alertes désactivées") { open(SettingsSection.RADARS) }

    SettingsGroupTitle("Lieux")
    val placesCount = listOfNotNull(saved.home, saved.work).size + saved.favorites.size
    SettingsNavigationRow("⭐", "Lieux enregistrés", "$placesCount") { open(SettingsSection.PLACES) }

    SettingsGroupTitle("Application")
    SettingsNavigationRow("📱", "À propos", null) { open(SettingsSection.ABOUT) }
}
