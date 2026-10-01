package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Bas gauche du HUD : vitesse GPS. Le macaron de limitation arrivera avec les données OSM. */
@Composable
fun SpeedPanel(speedKmh: Int, modifier: Modifier = Modifier) {
    CockpitPanel(modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = speedKmh.toString(), style = CockpitTypography.Speed, color = CockpitColors.Text)
            Text(text = "KM/H", style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
        }
    }
}
