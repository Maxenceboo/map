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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.navigation.trip.MissionPassedModel
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Écran d'arrivée façon GTA V : la carte s'assombrit et une fiche apparaît avec un léger rebond,
 * "Mission accomplie" en jaune, la destination, le bilan du trajet et le bouton pour terminer.
 */
@Composable
fun MissionPassedOverlay(model: MissionPassedModel, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        CockpitPanel(Modifier.fillMaxWidth().scale(scale.value), padding = 24.dp) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).background(CockpitColors.Accent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = CockpitColors.OnAccent, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Mission accomplie",
                    color = CockpitColors.Accent,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    model.destinationName,
                    style = CockpitTypography.Street,
                    color = CockpitColors.Text,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(22.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat(model.distance, "Distance")
                    Stat(model.duration, "Durée")
                    Stat(model.averageSpeed, "Moyenne")
                }
                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(HudShapes.Button)
                        .background(CockpitColors.Accent)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Terminer", style = CockpitTypography.Metric, color = CockpitColors.OnAccent)
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = CockpitTypography.Metric.copy(fontSize = 20.sp), color = CockpitColors.Text)
        Text(label, style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
    }
}
