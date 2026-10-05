package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Alertes de zone de danger et rappel des sources de données. */
@Composable
fun RadarSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsGroup {
        SettingsToggleRow(
            icon = Icons.Filled.Warning,
            title = "Alertes de zone de danger",
            description = "Bandeau, alerte sur l'écran de la voiture et bip à l'entrée d'une zone. L'emplacement des contrôles n'est pas affiché",
            checked = settings.radarAlerts,
        ) { enabled -> onUpdate { it.copy(radarAlerts = enabled) } }
    }
    SettingsGroup("Sources") {
        SettingsInfoRow(Icons.Filled.CheckCircle, "Base officielle", "Données publiques françaises embarquées, fonctionne hors ligne")
        SettingsInfoRow(HudIcons.Globe, "OpenStreetMap", "Complète la base quand le réseau le permet")
    }
}
