package com.gamemaps.irl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Thème Material sombre aligné sur la palette cockpit (toujours sombre, même de jour). */
@Composable
fun GameMapsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = CockpitColors.Route,
            background = CockpitColors.Black,
            surface = CockpitColors.Panel,
            onSurface = CockpitColors.Text,
            onBackground = CockpitColors.Text,
            error = CockpitColors.Danger,
        ),
        content = content,
    )
}
