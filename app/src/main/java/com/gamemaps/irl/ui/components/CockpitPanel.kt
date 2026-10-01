package com.gamemaps.irl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors

/**
 * Conteneur de base du HUD : flat, anguleux, sombre, bordure fine.
 * Pas d'ombre ni de flou (règle §2.1 : "pas de cards arrondies IA génériques").
 */
@Composable
fun CockpitPanel(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .background(CockpitColors.Panel, shape)
            .border(1.dp, CockpitColors.Border, shape)
            .padding(12.dp),
        content = content,
    )
}
