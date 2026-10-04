package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.location.GpsQuality
import com.gamemaps.irl.ui.theme.CockpitColors

/** Pastille de verrouillage GPS : vert < 10 m, jaune < 30 m, rouge sinon / en recherche. */
@Composable
fun GpsStatusDot(quality: GpsQuality, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(10.dp)
            .background(colorOf(quality), CircleShape)
            .semantics { contentDescription = "Signal GPS" },
    )
}

private fun colorOf(quality: GpsQuality): Color = when (quality) {
    GpsQuality.GOOD -> CockpitColors.GpsGood
    GpsQuality.FAIR -> CockpitColors.GpsFair
    GpsQuality.WEAK, GpsQuality.SEARCHING -> CockpitColors.Danger
}
