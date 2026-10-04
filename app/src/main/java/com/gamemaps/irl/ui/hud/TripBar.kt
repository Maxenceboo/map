package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Barre du bas pendant le guidage : bouton rouge pour arrêter, heure d'arrivée
 * avec le temps et la distance restants, et le bouton du son.
 */
@Composable
fun TripBar(hud: HudModel, isMuted: Boolean, onStop: () -> Unit, onToggleMute: () -> Unit, modifier: Modifier = Modifier) {
    CockpitPanel(modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundAction(CockpitColors.Danger, onStop) {
                Icon(Icons.Filled.Close, contentDescription = "Arrêter le guidage", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(hud.arrivalTime, style = CockpitTypography.Distance, color = CockpitColors.Text)
                Text("${hud.remainingDuration} · ${hud.remainingDistance}", style = CockpitTypography.Metric, color = CockpitColors.TextMuted)
            }
            RoundAction(CockpitColors.PanelRaised, onToggleMute) {
                Icon(
                    imageVector = if (isMuted) HudIcons.VolumeOff else HudIcons.VolumeOn,
                    contentDescription = if (isMuted) "Réactiver le son" else "Couper le son",
                    tint = if (isMuted) CockpitColors.Danger else CockpitColors.Text,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun RoundAction(background: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
