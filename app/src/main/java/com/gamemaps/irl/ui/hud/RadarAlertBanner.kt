package com.gamemaps.irl.ui.hud

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Bandeau rouge clignotant : "RADAR — 450 m" + vitesse contrôlée si connue (cahier des charges §6.2). */
@Composable
fun RadarAlertBanner(alert: RadarAlert, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    val transition = rememberInfiniteTransition(label = "radar")
    val border by transition.animateColor(
        initialValue = CockpitColors.Danger,
        targetValue = CockpitColors.Panel,
        animationSpec = infiniteRepeatable(tween(durationMillis = 500), RepeatMode.Reverse),
        label = "radar-border",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CockpitColors.Panel, shape)
            .border(2.dp, border, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("RADAR", style = CockpitTypography.Caption, color = CockpitColors.Danger)
            Text(DistanceFormatter.format(alert.distanceMeters), style = CockpitTypography.Distance, color = CockpitColors.Text)
        }
        alert.radar.maxSpeedKmh?.let {
            Spacer(Modifier.width(12.dp))
            SpeedLimitSign(it)
        }
    }
}
