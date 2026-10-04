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
import com.gamemaps.irl.data.speedlimit.SpeedLimit
import com.gamemaps.irl.navigation.SpeedingDetector
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Bas gauche : compteur rond avec la vitesse GPS ; le panneau de limitation s'y accroche en haut à droite.
 * Au-dessus d'une limitation renseignée, le compteur devient rouge ; au-dessus d'une limitation
 * seulement estimée, il devient orange (simple avertissement, sans bip).
 */
@Composable
fun SpeedGauge(speedKmh: Int, limit: SpeedLimit?, modifier: Modifier = Modifier) {
    val over = SpeedingDetector.isSpeeding(speedKmh, limit?.kmh)
    val background = when {
        !over -> CockpitColors.Panel
        limit?.estimated == true -> CockpitColors.Warning
        else -> CockpitColors.Danger
    }
    // Sur l'orange, du texte sombre reste lisible ; sur le rouge, du blanc.
    val onBackground = when (background) {
        CockpitColors.Panel -> CockpitColors.Text
        CockpitColors.Warning -> CockpitColors.OnAccent
        else -> Color.White
    }
    // La marge laisse la place au panneau qui dépasse du cercle.
    Box(modifier.padding(top = 14.dp, end = 14.dp)) {
        Box(Modifier.size(84.dp).hudSurface(CircleShape, background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(speedKmh.toString(), style = CockpitTypography.Speed, color = onBackground)
                Text("km/h", style = CockpitTypography.Caption, color = if (over) onBackground else CockpitColors.TextMuted)
            }
        }
        if (limit != null) {
            SpeedLimitSign(limit.kmh, Modifier.align(Alignment.TopEnd).offset(x = 14.dp, y = (-14).dp), estimated = limit.estimated)
        }
    }
}
