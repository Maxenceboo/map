package com.gamemaps.irl.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors

/**
 * Apparence commune des éléments posés sur la carte : fond opaque et ombre portée,
 * sans bordure. L'ombre les détache de la carte quel que soit le thème.
 */
fun Modifier.hudSurface(shape: Shape, color: Color = CockpitColors.Panel): Modifier =
    shadow(elevation = 8.dp, shape = shape, clip = false)
        .background(color, shape)
        .clip(shape)
