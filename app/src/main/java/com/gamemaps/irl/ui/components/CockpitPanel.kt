package com.gamemaps.irl.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors

/** Carte du HUD : surface arrondie posée sur la carte (voir [hudSurface]). */
@Composable
fun CockpitPanel(
    modifier: Modifier = Modifier,
    color: Color = CockpitColors.Panel,
    padding: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.hudSurface(HudShapes.Card, color).padding(padding),
        content = content,
    )
}

/** Formes partagées par le HUD. */
object HudShapes {
    val Card = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(16.dp)
    val Pill = RoundedCornerShape(50)
}
