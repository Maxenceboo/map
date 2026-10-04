package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Apparaît quand la carte a été déplacée au doigt : ramène la caméra sur le véhicule. */
@Composable
fun RecenterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Text(
        text = "◎ RECENTRER",
        style = CockpitTypography.Metric,
        color = CockpitColors.Route,
        modifier = modifier
            .background(CockpitColors.Panel, shape)
            .border(1.dp, CockpitColors.Route, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
