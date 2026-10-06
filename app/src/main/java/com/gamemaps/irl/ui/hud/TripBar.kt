package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Barre du bas pendant le guidage : bouton rouge pour arrêter, heure d'arrivée
 * avec le temps et la distance restants, et le bouton du son.
 *
 * Arrêter se fait en deux temps : la croix ([onAskStop]) remplace la barre par une question,
 * "Arrêter" ([onStop]) ou "Continuer" ([onCancelStop]).
 */
@Composable
fun TripBar(
    hud: HudModel,
    isMuted: Boolean,
    confirmStop: Boolean,
    onAskStop: () -> Unit,
    onCancelStop: () -> Unit,
    onStop: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CockpitPanel(modifier.fillMaxWidth(), padding = 12.dp) {
        if (confirmStop) StopQuestion(onCancelStop, onStop) else TripSummary(hud, isMuted, onAskStop, onToggleMute)
    }
}

@Composable
private fun TripSummary(hud: HudModel, isMuted: Boolean, onAskStop: () -> Unit, onToggleMute: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RoundAction(CockpitColors.Danger, onAskStop) {
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

@Composable
private fun StopQuestion(onContinue: () -> Unit, onStop: () -> Unit) {
    Column {
        Text("Arrêter le guidage ?", style = CockpitTypography.Street, color = CockpitColors.Text, modifier = Modifier.padding(start = 4.dp))
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WideAction("Continuer", CockpitColors.PanelRaised, CockpitColors.Text, onContinue, Modifier.weight(1f))
            WideAction("Arrêter", CockpitColors.Danger, Color.White, onStop, Modifier.weight(1f))
        }
    }
}

@Composable
private fun WideAction(label: String, background: Color, textColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.clip(HudShapes.Button).background(background).clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = CockpitTypography.Metric, color = textColor)
    }
}

@Composable
private fun RoundAction(background: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
