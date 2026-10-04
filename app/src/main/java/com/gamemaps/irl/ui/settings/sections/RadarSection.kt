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

/** Alertes radar et rappel des sources de données. */
@Composable
fun RadarSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsGroup {
        SettingsToggleRow(
            icon = Icons.Filled.Warning,
            title = "Alertes radar",
            description = "Bandeau, alerte sur l'écran de la voiture et bips à moins de 800 m devant",
            checked = settings.radarAlerts,
        ) { enabled -> onUpdate { it.copy(radarAlerts = enabled) } }
    }
    SettingsGroup("Sources") {
        SettingsInfoRow(Icons.Filled.CheckCircle, "Base officielle", "3 350 radars français embarqués, fonctionne hors ligne")
        SettingsInfoRow(HudIcons.Globe, "OpenStreetMap", "Complète la base (radars récents, étranger) quand le réseau le permet")
    }
}
