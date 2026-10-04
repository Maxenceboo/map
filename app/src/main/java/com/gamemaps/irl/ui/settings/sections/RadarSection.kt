package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Alertes radar et rappel des sources de données. */
@Composable
fun RadarSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsToggleRow(
        icon = "⚠",
        title = "Alertes radar",
        description = "Bandeau, alerte sur l'écran de la voiture et bips à moins de 800 m devant",
        checked = settings.radarAlerts,
    ) { enabled -> onUpdate { it.copy(radarAlerts = enabled) } }
    SettingsInfoRow("🇫🇷", "Base officielle", "3 350 radars français embarqués, fonctionne hors ligne")
    SettingsInfoRow("🌍", "OpenStreetMap", "Complète la base (radars récents, étranger) quand le réseau le permet")
}
