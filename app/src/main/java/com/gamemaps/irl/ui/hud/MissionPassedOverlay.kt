package com.gamemaps.irl.ui.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.navigation.trip.MissionPassedModel
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Écran d'arrivée façon GTA V : bandeau noir en travers de l'écran, "MISSION ACCOMPLIE"
 * en lettres dorées qui apparaissent avec un léger rebond, puis le bilan du trajet.
 * Un appui n'importe où ferme l'écran et termine le trajet.
 */
@Composable
fun MissionPassedOverlay(model: MissionPassedModel, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "MISSION ACCOMPLIE",
                color = GOLD,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale.value),
            )
            Text(model.destinationName, style = CockpitTypography.Street, color = CockpitColors.Text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat(model.distance, "DISTANCE")
                Stat(model.duration, "DURÉE")
                Stat(model.averageSpeed, "MOYENNE")
            }
            Spacer(Modifier.height(20.dp))
            Text("Touchez pour continuer", style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = CockpitTypography.Distance, color = CockpitColors.Text)
        Text(label, style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
    }
}

/** Or du "Mission Passed" de GTA V. */
private val GOLD = Color(0xFFF5B800)
