package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** État d'enregistrement de la destination affichée dans l'aperçu. */
data class PlaceSaveState(
    val isHome: Boolean,
    val isWork: Boolean,
    val isFavorite: Boolean,
)

/** Actions d'enregistrement de la destination. */
class PlaceSaveCallbacks(
    val onSetHome: () -> Unit,
    val onSetWork: () -> Unit,
    val onToggleFavorite: () -> Unit,
)

/** Trois boutons sous l'aperçu : définir comme Maison / Travail, ajouter aux favoris. Allumés si déjà le cas. */
@Composable
fun PlaceSaveActions(state: PlaceSaveState, callbacks: PlaceSaveCallbacks, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SaveToggle("🏠 Maison", state.isHome, callbacks.onSetHome, Modifier.weight(1f))
        SaveToggle("💼 Travail", state.isWork, callbacks.onSetWork, Modifier.weight(1f))
        SaveToggle(if (state.isFavorite) "★ Favori" else "☆ Favori", state.isFavorite, callbacks.onToggleFavorite, Modifier.weight(1f))
    }
}

@Composable
private fun SaveToggle(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .background(CockpitColors.Panel, shape)
            .border(1.dp, if (active) CockpitColors.Route else CockpitColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = CockpitTypography.Caption, color = if (active) CockpitColors.Route else CockpitColors.TextMuted)
    }
}
