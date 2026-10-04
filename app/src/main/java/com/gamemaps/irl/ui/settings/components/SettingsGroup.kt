package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Groupe de lignes du menu : un titre optionnel, puis une carte arrondie qui contient les lignes.
 * Les lignes d'un même sujet sont ainsi visuellement réunies, sans séparateurs.
 */
@Composable
fun SettingsGroup(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        title?.let {
            Text(it, style = CockpitTypography.Metric, color = CockpitColors.TextMuted, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp))
        }
        Column(
            modifier = Modifier.fillMaxWidth().clip(HudShapes.Card).background(CockpitColors.Panel).padding(vertical = 6.dp),
            content = content,
        )
    }
}
