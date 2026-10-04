package com.gamemaps.irl.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors

/** Bouton rond du HUD avec une icône (son, recentrer…). [description] est lu par les lecteurs d'écran. */
@Composable
fun HudIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = CockpitColors.Text,
    background: Color = CockpitColors.Panel,
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .hudSurface(CircleShape, background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(24.dp))
    }
}
