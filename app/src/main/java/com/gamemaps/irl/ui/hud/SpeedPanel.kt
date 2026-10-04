package com.gamemaps.irl.ui.hud

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.navigation.SpeedingDetector
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Bas gauche du HUD : vitesse GPS + panneau de limitation.
 * En excès de vitesse, le chiffre passe en rouge et clignote.
 */
@Composable
fun SpeedPanel(speedKmh: Int, limitKmh: Int?, modifier: Modifier = Modifier) {
    val speeding = SpeedingDetector.isSpeeding(speedKmh, limitKmh)
    CockpitPanel(modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = speedKmh.toString(),
                style = CockpitTypography.Speed,
                color = if (speeding) CockpitColors.Danger else CockpitColors.Text,
                modifier = if (speeding) Modifier.alpha(blinkingAlpha()) else Modifier,
            )
            Text(text = "KM/H", style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
            if (limitKmh != null) {
                Spacer(Modifier.height(8.dp))
                SpeedLimitSign(limitKmh)
            }
        }
    }
}

@Composable
private fun blinkingAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "speeding")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 450), RepeatMode.Reverse),
        label = "speeding-alpha",
    )
    return alpha
}
