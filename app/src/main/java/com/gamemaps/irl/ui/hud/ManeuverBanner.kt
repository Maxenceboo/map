package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Haut de l'écran pendant le guidage : la flèche sur fond jaune, la distance, puis l'instruction. */
@Composable
fun ManeuverBanner(hud: HudModel, modifier: Modifier = Modifier) {
    CockpitPanel(modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp).background(CockpitColors.Accent, HudShapes.Button),
                contentAlignment = Alignment.Center,
            ) {
                hud.maneuver?.let { ManeuverIcon(it, hud.roundaboutExit, CockpitColors.OnAccent, Modifier.size(46.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(text = hud.distanceToManeuver, style = CockpitTypography.Distance, color = CockpitColors.Text)
                Text(
                    text = if (hud.isRerouting) "Recalcul de l'itinéraire…" else hud.instruction,
                    style = CockpitTypography.Street,
                    color = if (hud.isRerouting) CockpitColors.Warning else CockpitColors.Text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
