package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Pastille "Favori" de l'aperçu : en jaune si la destination est déjà dans les favoris.
 * (Maison et Travail se définissent dans Paramètres > Lieux enregistrés.)
 */
@Composable
fun FavoriteToggle(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val content = if (isFavorite) CockpitColors.Accent else CockpitColors.TextMuted
    Row(
        modifier = modifier
            .clip(HudShapes.Pill)
            .background(if (isFavorite) CockpitColors.Accent.copy(alpha = 0.16f) else CockpitColors.PanelRaised)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(if (isFavorite) "Dans les favoris" else "Ajouter aux favoris", style = CockpitTypography.Caption, color = content)
    }
}
