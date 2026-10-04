package com.gamemaps.irl.ui.settings.sections

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsOptionRow
import com.gamemaps.irl.ui.settings.components.ThemePreview

/**
 * Choix du thème de carte, appliqué tout de suite au téléphone et à la voiture.
 * [themes] : les thèmes de l'app, suivis de ceux créés en mode développeur. Chaque ligne montre une vignette du thème.
 */
@Composable
fun ThemeSection(current: MapTheme, themes: List<MapTheme>, onSelect: (MapTheme) -> Unit) {
    val (custom, builtIn) = themes.partition { it.isCustom }
    ThemeGroup(null, builtIn, current, onSelect)
    if (custom.isNotEmpty()) ThemeGroup("Mes thèmes", custom, current, onSelect)
}

@Composable
private fun ThemeGroup(title: String?, themes: List<MapTheme>, current: MapTheme, onSelect: (MapTheme) -> Unit) {
    SettingsGroup(title) {
        themes.forEach { theme ->
            SettingsOptionRow(
                title = theme.label,
                description = theme.description,
                selected = theme.id == current.id,
                leading = { ThemePreview(theme.palette, Modifier.size(width = 72.dp, height = 52.dp)) },
                onClick = { onSelect(theme) },
            )
        }
    }
}
