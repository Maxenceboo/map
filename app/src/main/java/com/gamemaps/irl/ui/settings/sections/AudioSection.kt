package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Son général et détail de ce que l'application fait entendre. */
@Composable
fun AudioSection(
    settings: AppSettings,
    isMuted: Boolean,
    onToggleMuted: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    SettingsToggleRow("🔊", "Son", "Coupe tout d'un coup (aussi accessible depuis la carte)", !isMuted) { onToggleMuted() }
    SettingsToggleRow("🗣", "Guidage vocal", "\"Dans 500 mètres, tournez à droite…\"", settings.voiceGuidance) { enabled ->
        onUpdate { it.copy(voiceGuidance = enabled) }
    }
    SettingsToggleRow("📸", "Bips radar", "Double bip, puis triple bip sous 300 m", settings.radarBeeps) { enabled ->
        onUpdate { it.copy(radarBeeps = enabled) }
    }
    SettingsToggleRow("🚨", "Bip d'excès de vitesse", "Au-delà de la limite + 3 km/h", settings.speedingBeep) { enabled ->
        onUpdate { it.copy(speedingBeep = enabled) }
    }
}
