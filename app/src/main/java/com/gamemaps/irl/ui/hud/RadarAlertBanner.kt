package com.gamemaps.irl.ui.hud

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Bandeau "Zone de danger" (cahier des charges §6.2), affiché tant qu'on est dans la zone.
 * Il rappelle la vitesse autorisée, mais ne donne ni l'emplacement ni la distance du contrôle.
 */
@Composable
fun RadarAlertBanner(alert: RadarAlert, modifier: Modifier = Modifier) {
    CockpitPanel(modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(CockpitColors.Warning, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = CockpitColors.OnAccent, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Zone de danger", style = CockpitTypography.Street, color = CockpitColors.Text)
                Text("Restez vigilant", style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
            }
            alert.maxSpeedKmh?.let {
                Spacer(Modifier.width(12.dp))
                SpeedLimitSign(it)
            }
        }
    }
}
