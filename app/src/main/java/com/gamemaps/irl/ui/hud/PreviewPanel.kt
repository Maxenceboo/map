package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Aperçu avant le départ : destination, distance, durée, heure d'arrivée,
 * enregistrement (Maison / Travail / Favori), bouton DÉMARRER pleine largeur (règle §2.3) et ANNULER.
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
    CockpitPanel(modifier.fillMaxWidth()) {
        Column {
            Text(preview.destinationName, style = CockpitTypography.Street, color = CockpitColors.Text)
            if (preview.destinationSubtitle.isNotBlank()) {
                Text(preview.destinationSubtitle, style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric(preview.duration, "DURÉE")
                Metric(preview.distance, "DISTANCE")
                Metric(preview.arrivalTime, "ARRIVÉE")
            }
            Spacer(Modifier.height(12.dp))
            PlaceSaveActions(saveState, saveCallbacks)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("ANNULER", CockpitColors.Panel, CockpitColors.TextMuted, onCancel, Modifier.weight(1f))
                ActionButton("DÉMARRER", CockpitColors.Route, Color.Black, onStart, Modifier.weight(2f))
            }
        }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column {
        Text(value, style = CockpitTypography.Metric, color = CockpitColors.Text)
        Text(label, style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
    }
}

@Composable
private fun ActionButton(text: String, background: Color, textColor: Color, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .background(background, shape)
            .border(1.dp, CockpitColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = CockpitTypography.Metric, color = textColor)
    }
}
