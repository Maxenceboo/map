package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Petite note d'état posée sur la carte : "Aucun résultat…", "Recherche du signal GPS…". */
@Composable
fun HudNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = CockpitTypography.Caption,
        color = CockpitColors.TextMuted,
        modifier = modifier.hudSurface(HudShapes.Pill).padding(horizontal = 16.dp, vertical = 10.dp),
    )
}
