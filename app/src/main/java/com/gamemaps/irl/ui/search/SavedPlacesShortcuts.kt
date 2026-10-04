package com.gamemaps.irl.ui.search

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Raccourcis sous la barre de recherche : Maison et Travail (toujours proposés), puis les favoris.
 * Un appui lance directement l'itinéraire (cahier des charges §8 : "trajet en 1 clic").
 * Tant que Maison ou Travail n'est pas défini, la pastille est grisée et explique comment faire.
 */
@Composable
fun SavedPlacesShortcuts(saved: SavedPlaces, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shortcuts = buildList {
        add(Shortcut(Icons.Filled.Home, "Maison", saved.home))
        add(Shortcut(HudIcons.Work, "Travail", saved.work))
        saved.favorites.forEach { add(Shortcut(Icons.Filled.Star, it.name, it)) }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        // La marge laisse la place à l'ombre des pastilles.
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(shortcuts, key = { it.label + it.place?.id }) { shortcut ->
            Chip(shortcut) {
                val place = shortcut.place
                if (place != null) {
                    onSelect(place)
                } else {
                    val hint = "Cherchez l'adresse, puis touchez ${shortcut.label} dans l'aperçu du trajet"
                    Toast.makeText(context, hint, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

/** @property place null si le lieu (Maison, Travail) n'est pas encore défini. */
private class Shortcut(val icon: ImageVector, val label: String, val place: Place?)

@Composable
private fun Chip(shortcut: Shortcut, onClick: () -> Unit) {
    val defined = shortcut.place != null
    Row(
        modifier = Modifier
            .hudSurface(HudShapes.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(shortcut.icon, contentDescription = null, tint = if (defined) CockpitColors.Accent else CockpitColors.TextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(shortcut.label, style = CockpitTypography.Metric, color = if (defined) CockpitColors.Text else CockpitColors.TextMuted, maxLines = 1)
    }
}
