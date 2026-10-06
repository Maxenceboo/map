package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Choix du trajet dans l'aperçu : une case par trajet proposé (durée, distance, nature) ; la case choisie est cerclée de jaune. */
@Composable
fun RouteOptionChips(options: List<RouteOption>, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, option ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(HudShapes.Button)
                    .background(if (option.selected) CockpitColors.Accent.copy(alpha = 0.16f) else CockpitColors.PanelRaised)
                    .border(2.dp, if (option.selected) CockpitColors.Accent else Color.Transparent, HudShapes.Button)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(option.duration, style = CockpitTypography.Metric, color = if (option.selected) CockpitColors.Accent else CockpitColors.Text, maxLines = 1)
                Text(option.distance, style = CockpitTypography.Caption, color = CockpitColors.Text, maxLines = 1)
                Text(option.label, style = CockpitTypography.Caption, color = CockpitColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
