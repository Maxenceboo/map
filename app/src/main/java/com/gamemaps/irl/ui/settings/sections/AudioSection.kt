package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Son général et détail de ce que l'application fait entendre. */
@Composable
fun AudioSection(
    settings: AppSettings,
    isMuted: Boolean,
    onToggleMuted: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    SettingsGroup {
        SettingsToggleRow(HudIcons.VolumeOn, "Son", "Coupe tout d'un coup (aussi accessible depuis la carte)", !isMuted) { onToggleMuted() }
    }
    SettingsGroup("Ce qui se fait entendre") {
        SettingsToggleRow(HudIcons.Speech, "Guidage vocal", "« Dans 500 mètres, tournez à droite… »", settings.voiceGuidance) { enabled ->
            onUpdate { it.copy(voiceGuidance = enabled) }
        }
        SettingsToggleRow(HudIcons.Radar, "Bip de zone de danger", "Double bip à l'entrée d'une zone", settings.radarBeeps) { enabled ->
            onUpdate { it.copy(radarBeeps = enabled) }
        }
        SettingsToggleRow(Icons.Filled.Notifications, "Bip d'excès de vitesse", "Au-delà de la limite + 3 km/h", settings.speedingBeep) { enabled ->
            onUpdate { it.copy(speedingBeep = enabled) }
        }
    }
}
