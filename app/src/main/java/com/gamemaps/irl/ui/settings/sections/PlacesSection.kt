package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.settings.components.SettingsGroupTitle
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow

/**
 * Maison, Travail et favoris, avec possibilité de les retirer.
 * Pour en ajouter : chercher un lieu, puis 🏠 / 💼 / ☆ dans l'aperçu du trajet.
 */
@Composable
fun PlacesSection(
    saved: SavedPlaces,
    onClearHome: () -> Unit,
    onClearWork: () -> Unit,
    onRemoveFavorite: (Place) -> Unit,
) {
    SettingsGroupTitle("Raccourcis")
    PlaceRow("🏠", "Maison", saved.home, onClearHome)
    PlaceRow("💼", "Travail", saved.work, onClearWork)

    SettingsGroupTitle("Favoris")
    if (saved.favorites.isEmpty()) {
        SettingsInfoRow("☆", "Aucun favori", "Cherchez un lieu puis touchez ☆ Favori dans l'aperçu du trajet")
    }
    saved.favorites.forEach { place ->
        SettingsInfoRow("★", place.name, place.subtitle.ifBlank { null }, "RETIRER") { onRemoveFavorite(place) }
    }
}

@Composable
private fun PlaceRow(icon: String, label: String, place: Place?, onClear: () -> Unit) {
    if (place == null) {
        SettingsInfoRow(icon, label, "Non défini — choisissez-le depuis l'aperçu d'un trajet")
    } else {
        SettingsInfoRow(icon, label, listOf(place.name, place.subtitle).filter { it.isNotBlank() }.joinToString(" · "), "RETIRER", onClear)
    }
}
