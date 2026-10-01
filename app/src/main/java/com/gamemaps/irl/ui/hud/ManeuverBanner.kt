package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Haut gauche du HUD : grosse flèche, distance avant la manœuvre, nom de la voie. */
@Composable
fun ManeuverBanner(hud: HudModel, modifier: Modifier = Modifier) {
    CockpitPanel(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = hud.maneuverGlyph, color = CockpitColors.Route, fontSize = 48.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = hud.distanceToManeuver, style = CockpitTypography.Distance, color = CockpitColors.Text)
                Text(
                    text = if (hud.isRerouting) "Recalcul de l'itinéraire…" else hud.instruction,
                    style = CockpitTypography.Street,
                    color = if (hud.isRerouting) CockpitColors.Warning else CockpitColors.TextMuted,
                )
            }
        }
    }
}
