package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.Perspective
import com.gamemaps.irl.ui.settings.components.SettingsOptionRow

/** Caméra inclinée derrière le véhicule, ou vue de dessus. */
@Composable
fun PerspectiveSection(current: Perspective, onSelect: (Perspective) -> Unit) {
    Perspective.entries.forEach { perspective ->
        SettingsOptionRow(
            title = perspective.label,
            description = when (perspective) {
                Perspective.COCKPIT_3D -> "Caméra inclinée à 55° derrière le véhicule"
                Perspective.TOP_DOWN_2D -> "Carte vue du ciel, plus de route visible autour"
            },
            selected = perspective == current,
            onClick = { onSelect(perspective) },
        )
    }
}
