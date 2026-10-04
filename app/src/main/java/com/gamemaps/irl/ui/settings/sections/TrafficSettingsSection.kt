package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow
import com.gamemaps.irl.ui.settings.components.TomTomKeyField

/**
 * Trafic en temps réel (TomTom) : interrupteur, puis la clé d'API.
 *
 * @param maskedKey clé enregistrée, masquée ("••••••••a1b2"), ou null s'il n'y en a pas.
 */
@Composable
fun TrafficSettingsSection(
    settings: AppSettings,
    maskedKey: String?,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onSaveKey: (String) -> Boolean,
    onClearKey: () -> Unit,
) {
    SettingsToggleRow(
        icon = "🚦",
        title = "Trafic en temps réel",
        description = "Itinéraire qui évite les bouchons ; bords orange (ralenti) ou rouges (bouchon) sur le tracé",
        checked = settings.traffic,
    ) { enabled -> onUpdate { it.copy(traffic = enabled) } }

    if (maskedKey != null) {
        SettingsInfoRow("🔑", "Clé TomTom enregistrée", maskedKey, actionLabel = "Retirer", onAction = onClearKey)
        SettingsInfoRow("📱", "Stockée sur ce téléphone", "Ni dans l'application ni dans les sauvegardes. Si TomTom la refuse, l'itinéraire est calculé sans trafic")
    } else {
        SettingsInfoRow("🔑", "Aucune clé TomTom", "Créez une clé gratuite sur developer.tomtom.com, copiez-la puis collez-la ici. Sans clé : itinéraires sans trafic")
        TomTomKeyField(onSaveKey)
    }
}
