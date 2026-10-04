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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Aperçu avant le départ : destination, durée en grand, distance et heure d'arrivée,
 * croix pour annuler en haut à droite, enregistrement (Maison / Travail / Favori), puis Démarrer (règle §2.3 : bouton principal large).
 */
@Composable
fun PreviewPanel(
    preview: PreviewModel,
    saveState: PlaceSaveState,
    saveCallbacks: PlaceSaveCallbacks,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CockpitPanel(modifier.fillMaxWidth(), padding = 18.dp) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(preview.destinationName, style = CockpitTypography.Street, color = CockpitColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (preview.destinationSubtitle.isNotBlank()) {
                        Text(preview.destinationSubtitle, style = CockpitTypography.Caption, color = CockpitColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Annuler le trajet",
                    tint = CockpitColors.Text,
                    modifier = Modifier.padding(start = 12.dp).clip(CircleShape).background(CockpitColors.PanelRaised).clickable(onClick = onCancel).padding(8.dp).size(22.dp),
                )
            }
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(preview.duration, style = CockpitTypography.Distance, color = CockpitColors.Accent)
                Spacer(Modifier.width(12.dp))
                Text(
                    "${preview.distance} · arrivée ${preview.arrivalTime}",
                    style = CockpitTypography.Metric,
                    color = CockpitColors.TextMuted,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
            preview.trafficDelay?.let { delay ->
                Text(delay, style = CockpitTypography.Caption, color = CockpitColors.Warning)
            }
            Spacer(Modifier.height(16.dp))

            PlaceSaveActions(saveState, saveCallbacks)
            Spacer(Modifier.height(12.dp))

            ActionButton("Démarrer", CockpitColors.Accent, CockpitColors.OnAccent, onStart, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ActionButton(text: String, background: Color, textColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(HudShapes.Button)
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = CockpitTypography.Metric, color = textColor)
    }
}
