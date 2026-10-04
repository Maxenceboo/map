package com.gamemaps.irl.data.search.ranking

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind
import org.junit.Assert.assertEquals
import org.junit.Test

class RankingTest {

    private val bordeaux = LatLng(44.8378, -0.5792)

    private fun poi(id: String, name: String, category: String, position: LatLng) =
        Place(id, name, "Bordeaux", position, PlaceKind.POI, category)

    @Test
    fun `normalisation - accents, majuscules et tirets ignorés`() {
        assertEquals("gare saint jean", TextNormalizer.normalize("Gare Saint-Jean"))
        assertEquals("cafe de l etoile", TextNormalizer.normalize("Café de l’Étoile"))
        assertEquals(listOf("rue", "sainte", "catherine"), TextNormalizer.tokens("  Rue Sainte-Catherine "))
    }

    @Test
    fun `doublons - on garde la gare plutôt que l'arrêt de tram du même nom`() {
        val tram = poi("t", "Gare Saint-Jean", "railway:tram_stop", LatLng(44.8259, -0.5568))
        val station = poi("s", "Gare Saint-Jean", "railway:station", LatLng(44.8256, -0.5560))
        val elsewhere = poi("e", "Gare Saint-Jean", "railway:station", LatLng(43.6, 1.44)) // Toulouse : pas un doublon
        val result = PlaceDeduplicator.deduplicate(listOf(tram, station, elsewhere))
        assertEquals(setOf("s", "e"), result.map { it.id }.toSet())
    }

    @Test
    fun `doublons - une ville décrite par plusieurs objets n'apparaît qu'une fois`() {
        val center = Place("c", "Bordeaux", "33000 Bordeaux", LatLng(44.8378, -0.5792), PlaceKind.CITY)
        val boundary = Place("b", "Bordeaux", "", LatLng(44.8590, -0.5730), PlaceKind.CITY) // ~2,4 km plus loin
        val namesake = Place("n", "Bordeaux", "40090 Cère", LatLng(44.0, -0.53), PlaceKind.CITY) // lieu-dit des Landes
        val result = PlaceDeduplicator.deduplicate(listOf(center, boundary, namesake))
        assertEquals(setOf("c", "n"), result.map { it.id }.toSet())
    }

    @Test
    fun `à texte égal, le plus proche passe devant`() {
        val far = poi("far", "Leclerc", "shop:supermarket", LatLng(45.75, 4.85)) // Lyon
        val near = poi("near", "Leclerc", "shop:supermarket", LatLng(44.85, -0.60))
        assertEquals(listOf("near", "far"), PlaceRanker.rank("leclerc", listOf(far, near), bordeaux).map { it.id })
    }

    @Test
    fun `le texte compte plus que la distance pour un nom différent`() {
        val match = poi("match", "Gare Saint-Jean", "railway:station", LatLng(44.8256, -0.5560))
        val nearbyOther = poi("other", "Boulangerie du coin", "shop:bakery", bordeaux)
        assertEquals("match", PlaceRanker.rank("gare saint jean", listOf(nearbyOther, match), bordeaux).first().id)
    }

    @Test
    fun `une requête avec numéro favorise l'adresse précise`() {
        val address = Place("a", "12 Rue Sainte-Catherine", "33000 Bordeaux", bordeaux, PlaceKind.ADDRESS)
        val street = Place("s", "Rue Sainte-Catherine", "33000 Bordeaux", bordeaux, PlaceKind.STREET)
        assertEquals("a", PlaceRanker.rank("12 rue sainte catherine", listOf(street, address), bordeaux).first().id)
    }
}
