package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow

/**
 * Maison, Travail et favoris, avec possibilité de les retirer.
 * Pour en ajouter : chercher un lieu, puis Maison / Travail / Favori dans l'aperçu du trajet.
 */
@Composable
fun PlacesSection(
    saved: SavedPlaces,
    onClearHome: () -> Unit,
    onClearWork: () -> Unit,
    onRemoveFavorite: (Place) -> Unit,
) {
    SettingsGroup("Raccourcis") {
        PlaceRow(Icons.Filled.Home, "Maison", saved.home, onClearHome)
        PlaceRow(HudIcons.Work, "Travail", saved.work, onClearWork)
    }

    SettingsGroup("Favoris") {
        if (saved.favorites.isEmpty()) {
            SettingsInfoRow(Icons.Filled.Star, "Aucun favori", "Cherchez un lieu puis touchez Favori dans l'aperçu du trajet")
        }
        saved.favorites.forEach { place ->
            SettingsInfoRow(Icons.Filled.Star, place.name, place.subtitle.ifBlank { null }, "Retirer") { onRemoveFavorite(place) }
        }
    }
}

@Composable
private fun PlaceRow(icon: ImageVector, label: String, place: Place?, onClear: () -> Unit) {
    if (place == null) {
        SettingsInfoRow(icon, label, "Non défini : choisissez-le depuis l'aperçu d'un trajet")
    } else {
        SettingsInfoRow(icon, label, listOf(place.name, place.subtitle).filter { it.isNotBlank() }.joinToString(" · "), "Retirer", onClear)
    }
}
