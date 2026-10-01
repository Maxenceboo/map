package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Bas droite du HUD : heure d'arrivée, distance et temps restants, bouton d'arrêt. */
@Composable
fun ArrivalPanel(hud: HudModel, onStop: () -> Unit, modifier: Modifier = Modifier) {
    CockpitPanel(modifier) {
        Column(horizontalAlignment = Alignment.End) {
            Text(text = hud.arrivalTime, style = CockpitTypography.Distance, color = CockpitColors.Text)
            Text(text = hud.remainingDistance, style = CockpitTypography.Metric, color = CockpitColors.TextMuted)
            Text(text = hud.remainingDuration, style = CockpitTypography.Metric, color = CockpitColors.TextMuted)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "ARRÊTER",
                style = CockpitTypography.Caption,
                color = CockpitColors.Danger,
                modifier = Modifier.clickable(onClick = onStop),
            )
        }
    }
}
