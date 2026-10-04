package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.SettingsSection
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow

/**
 * Maison et Travail (un appui ouvre l'écran pour les choisir), puis les favoris.
 * Les favoris s'ajoutent depuis l'aperçu d'un trajet.
 */
@Composable
fun PlacesSection(saved: SavedPlaces, open: (SettingsSection) -> Unit, onRemoveFavorite: (Place) -> Unit) {
    SettingsGroup("Trajets en un appui") {
        SettingsNavigationRow(Icons.Filled.Home, "Maison", saved.home?.name ?: "À définir") { open(SettingsSection.PLACE_HOME) }
        SettingsNavigationRow(HudIcons.Work, "Travail", saved.work?.name ?: "À définir") { open(SettingsSection.PLACE_WORK) }
    }

    SettingsGroup("Favoris") {
        if (saved.favorites.isEmpty()) {
            SettingsInfoRow(Icons.Filled.Star, "Aucun favori", "Cherchez un lieu puis touchez « Ajouter aux favoris » dans l'aperçu du trajet")
        }
        saved.favorites.forEach { place ->
            SettingsInfoRow(Icons.Filled.Star, place.name, place.subtitle.ifBlank { null }, "Retirer") { onRemoveFavorite(place) }
        }
    }
}
