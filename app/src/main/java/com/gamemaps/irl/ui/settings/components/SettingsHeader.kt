package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** En-tête de chaque écran du menu : "← Retour" en haut à gauche (règle §2.3), puis le titre. */
@Composable
fun SettingsHeader(title: String, backLabel: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "← $backLabel",
            style = CockpitTypography.Metric,
            color = CockpitColors.Route,
            modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 16.dp, vertical = 14.dp),
        )
        Text(
            text = title,
            style = CockpitTypography.Distance,
            color = CockpitColors.Text,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )
        HorizontalDivider(color = CockpitColors.Border, thickness = 1.dp)
    }
}
