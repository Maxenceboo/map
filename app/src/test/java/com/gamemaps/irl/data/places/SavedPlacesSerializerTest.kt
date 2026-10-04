package com.gamemaps.irl.data.places

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPlacesSerializerTest {

    private val home = Place("ban_1", "12 Rue Sainte-Catherine", "33000 Bordeaux", LatLng(44.84, -0.57), PlaceKind.ADDRESS)
    private val station = Place("photon_N1", "Gare Saint-Jean", "33800 Bordeaux", LatLng(44.8256, -0.556), PlaceKind.POI, "railway:station")

    @Test
    fun `aller-retour JSON sans perte`() {
        val saved = SavedPlaces(home = home, work = null, favorites = listOf(station))
        assertEquals(saved, SavedPlacesSerializer.fromJson(SavedPlacesSerializer.toJson(saved)))
    }

    @Test
    fun `contenu vide ou corrompu - aucun lieu, pas de plantage`() {
        assertTrue(SavedPlacesSerializer.fromJson(null).isEmpty)
        assertTrue(SavedPlacesSerializer.fromJson("pas du json").isEmpty)
    }

    @Test
    fun `favori reconnu par son identifiant`() {
        val saved = SavedPlaces(favorites = listOf(station))
        assertTrue(saved.isFavorite(station.copy(name = "Autre nom")))
    }
}
