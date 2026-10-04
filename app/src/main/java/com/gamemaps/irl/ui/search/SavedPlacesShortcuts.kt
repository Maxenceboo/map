package com.gamemaps.irl.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Raccourcis sous la barre de recherche : Maison, Travail, puis les favoris.
 * Un appui lance directement l'itinéraire (cahier des charges §8 : "trajet en 1 clic").
 */
@Composable
fun SavedPlacesShortcuts(saved: SavedPlaces, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    val shortcuts = buildList {
        saved.home?.let { add("🏠 Maison" to it) }
        saved.work?.let { add("💼 Travail" to it) }
        saved.favorites.forEach { add("★ ${it.name}" to it) }
    }
    if (shortcuts.isEmpty()) return
    LazyRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(shortcuts, key = { (label, place) -> label + place.id }) { (label, place) ->
            Chip(label, onClick = { onSelect(place) })
        }
    }
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .background(CockpitColors.Panel, shape)
            .border(1.dp, CockpitColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = CockpitTypography.Metric, color = CockpitColors.Text, maxLines = 1)
    }
}
