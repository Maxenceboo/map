package com.gamemaps.irl.data.search

import com.gamemaps.irl.core.geo.LatLng
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class HybridPlaceSearchTest {

    private val here = LatLng(44.84, -0.58)
    private val address = Place("ban_1", "Rue de la Gare", "33000 Bordeaux", LatLng(44.84, -0.57), PlaceKind.STREET)
    private val station = Place("photon_1", "Gare Saint-Jean", "33800 Bordeaux", LatLng(44.8256, -0.5560), PlaceKind.POI, "railway:station")

    @Test
    fun `fusionne les deux moteurs`() = runTest {
        val search = HybridPlaceSearch(listOf(PlaceSearch { _, _ -> listOf(address) }, PlaceSearch { _, _ -> listOf(station) }))
        val ids = search.search("gare saint jean", here).map { it.id }
        assertEquals(listOf("photon_1", "ban_1"), ids) // la gare correspond mieux au texte
    }

    @Test
    fun `un moteur en panne n'empêche pas les résultats de l'autre`() = runTest {
        val search = HybridPlaceSearch(listOf(PlaceSearch { _, _ -> throw IOException("BAN hors ligne") }, PlaceSearch { _, _ -> listOf(station) }))
        assertEquals(listOf(station), search.search("gare", here))
    }

    @Test(expected = IOException::class)
    fun `tous les moteurs en panne - l'erreur remonte`() = runTest {
        HybridPlaceSearch(listOf(PlaceSearch { _, _ -> throw IOException("hors ligne") })).search("gare", here)
    }

    @Test
    fun `limité au nombre maximal de résultats`() = runTest {
        val many = (1..20).map { Place("p$it", "Lieu $it", "", LatLng(44.84 + it * 0.01, -0.58)) }
        assertEquals(5, HybridPlaceSearch(listOf(PlaceSearch { _, _ -> many }), maxResults = 5).search("lieu", here).size)
    }
}
