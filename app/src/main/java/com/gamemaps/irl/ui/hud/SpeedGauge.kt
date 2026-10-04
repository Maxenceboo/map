package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.navigation.SpeedingDetector
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Bas gauche : compteur rond avec la vitesse GPS ; le panneau de limitation s'y accroche en haut à droite.
 * En excès de vitesse, le compteur devient rouge.
 */
@Composable
fun SpeedGauge(speedKmh: Int, limitKmh: Int?, modifier: Modifier = Modifier) {
    val speeding = SpeedingDetector.isSpeeding(speedKmh, limitKmh)
    // La marge laisse la place au panneau qui dépasse du cercle.
    Box(modifier.padding(top = 14.dp, end = 14.dp)) {
        Box(
            modifier = Modifier.size(84.dp).hudSurface(CircleShape, if (speeding) CockpitColors.Danger else CockpitColors.Panel),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(speedKmh.toString(), style = CockpitTypography.Speed, color = if (speeding) Color.White else CockpitColors.Text)
                Text("km/h", style = CockpitTypography.Caption, color = if (speeding) Color.White else CockpitColors.TextMuted)
            }
        }
        if (limitKmh != null) {
            SpeedLimitSign(limitKmh, Modifier.align(Alignment.TopEnd).offset(x = 14.dp, y = (-14).dp))
        }
    }
}
