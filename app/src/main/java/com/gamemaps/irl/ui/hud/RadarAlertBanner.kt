package com.gamemaps.irl.ui.hud

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.navigation.radar.RadarAlertLevel
import com.gamemaps.irl.navigation.radar.RadarTypeText
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Alerte radar (cahier des charges §6.2) : type, route, distance, vitesse contrôlée.
 * - Avertissement : carte sombre, pastille rouge.
 * - Urgent (< 300 m) : toute la carte clignote en rouge.
 */
@Composable
fun RadarAlertBanner(alert: RadarAlert, modifier: Modifier = Modifier) {
    val urgent = alert.level == RadarAlertLevel.URGENT
    val transition = rememberInfiniteTransition(label = "radar")
    val pulse by transition.animateColor(
        initialValue = CockpitColors.Danger,
        targetValue = CockpitColors.Panel,
        animationSpec = infiniteRepeatable(tween(durationMillis = 350), RepeatMode.Reverse),
        label = "radar-pulse",
    )

    CockpitPanel(modifier.fillMaxWidth(), color = if (urgent) pulse else CockpitColors.Panel, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(if (urgent) Color.White else CockpitColors.Danger, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = if (urgent) CockpitColors.Danger else Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(DistanceFormatter.format(alert.distanceMeters), style = CockpitTypography.Distance, color = CockpitColors.Text)
                Text(title(alert), style = CockpitTypography.Metric, color = if (urgent) Color.White else CockpitColors.TextMuted)
            }
            alert.radar.maxSpeedKmh?.let {
                Spacer(Modifier.width(12.dp))
                SpeedLimitSign(it)
            }
        }
    }
}

/** "Radar feu rouge · A10" : le type en minuscules (première lettre exceptée), puis la route. */
private fun title(alert: RadarAlert): String {
    val type = RadarTypeText.banner(alert.radar.type).lowercase().replaceFirstChar { it.uppercase() }
    return listOfNotNull(type, alert.radar.road).joinToString(" · ")
}
