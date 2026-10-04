package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.routing.tomtom.TomTomApiKey
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Trafic en temps réel (TomTom) : interrupteur et état de la clé d'API. */
@Composable
fun TrafficSettingsSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsToggleRow(
        icon = "🚦",
        title = "Trafic en temps réel",
        description = "Itinéraire qui évite les bouchons ; bords orange (ralenti) ou rouges (bouchon) sur le tracé",
        checked = settings.traffic,
    ) { enabled -> onUpdate { it.copy(traffic = enabled) } }

    if (TomTomApiKey.isConfigured) {
        SettingsInfoRow("🔑", "Clé TomTom installée", "Sans réseau ou si TomTom refuse la clé, l'itinéraire est calculé sans trafic")
    } else {
        SettingsInfoRow("🔑", "Aucune clé TomTom", "Ajoutez tomtom.apiKey=… dans local.properties puis recompilez. En attendant : itinéraires sans trafic")
    }
}
