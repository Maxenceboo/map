package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.icons.HudIcons
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

/** Trois pastilles sous l'aperçu : Maison, Travail, Favori. En jaune si la destination l'est déjà. */
@Composable
fun PlaceSaveActions(state: PlaceSaveState, callbacks: PlaceSaveCallbacks, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SaveToggle(Icons.Filled.Home, "Maison", state.isHome, callbacks.onSetHome, Modifier.weight(1f))
        SaveToggle(HudIcons.Work, "Travail", state.isWork, callbacks.onSetWork, Modifier.weight(1f))
        SaveToggle(Icons.Filled.Star, "Favori", state.isFavorite, callbacks.onToggleFavorite, Modifier.weight(1f))
    }
}

@Composable
private fun SaveToggle(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val content = if (active) CockpitColors.Accent else CockpitColors.TextMuted
    Row(
        modifier = modifier
            .clip(HudShapes.Pill)
            .background(if (active) CockpitColors.Accent.copy(alpha = 0.16f) else CockpitColors.PanelRaised)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = CockpitTypography.Caption, color = content)
    }
}
