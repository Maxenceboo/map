package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.ui.settings.components.SettingsOptionRow

/** Choix du thème de carte, appliqué tout de suite au téléphone et à la voiture. */
@Composable
fun ThemeSection(current: MapTheme, onSelect: (MapTheme) -> Unit) {
    MapTheme.entries.forEach { theme ->
        SettingsOptionRow(
            title = theme.label,
            description = descriptionOf(theme),
            selected = theme == current,
            onClick = { onSelect(theme) },
        )
    }
}

private fun descriptionOf(theme: MapTheme): String = when (theme) {
    MapTheme.GTA_RADAR -> "Bleu nuit, routes ardoise, itinéraire violet"
    MapTheme.WAZE_NIGHT -> "Noir profond, grands axes cyan, itinéraire bleu électrique"
}
