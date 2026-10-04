package com.gamemaps.irl.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Raccourcis sous la barre de recherche : Maison, Travail, puis les favoris.
 * Un appui lance directement l'itinéraire (cahier des charges §8 : "trajet en 1 clic").
 */
@Composable
fun SavedPlacesShortcuts(saved: SavedPlaces, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    val shortcuts = buildList {
        saved.home?.let { add(Shortcut(Icons.Filled.Home, "Maison", it)) }
        saved.work?.let { add(Shortcut(HudIcons.Work, "Travail", it)) }
        saved.favorites.forEach { add(Shortcut(Icons.Filled.Star, it.name, it)) }
    }
    if (shortcuts.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        // La marge laisse la place à l'ombre des pastilles.
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(shortcuts, key = { it.label + it.place.id }) { shortcut ->
            Chip(shortcut, onClick = { onSelect(shortcut.place) })
        }
    }
}

private class Shortcut(val icon: ImageVector, val label: String, val place: Place)

@Composable
private fun Chip(shortcut: Shortcut, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .hudSurface(HudShapes.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(shortcut.icon, contentDescription = null, tint = CockpitColors.Accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(shortcut.label, style = CockpitTypography.Metric, color = CockpitColors.Text, maxLines = 1)
    }
}
