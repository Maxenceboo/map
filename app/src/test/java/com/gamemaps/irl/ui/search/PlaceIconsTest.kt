package com.gamemaps.irl.ui.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind
import com.gamemaps.irl.ui.icons.HudIcons
import org.junit.Assert.assertSame
import org.junit.Test

class PlaceIconsTest {

    private fun poi(category: String?) = Place("id", "Lieu", "", LatLng(44.84, -0.58), PlaceKind.POI, category)

    @Test
    fun `une icône par grande catégorie`() {
        assertSame(HudIcons.Train, PlaceIcons.iconFor(poi("railway:station")))
        assertSame(HudIcons.Fuel, PlaceIcons.iconFor(poi("amenity:fuel")))
        assertSame(HudIcons.Restaurant, PlaceIcons.iconFor(poi("amenity:fast_food")))
        assertSame(HudIcons.Hotel, PlaceIcons.iconFor(poi("tourism:hotel")))
        assertSame(HudIcons.Parking, PlaceIcons.iconFor(poi("amenity:parking")))
        assertSame(Icons.Filled.ShoppingCart, PlaceIcons.iconFor(poi("shop:supermarket")))
    }

    @Test
    fun `catégorie inconnue ou absente - le repère de lieu`() {
        assertSame(Icons.Filled.Place, PlaceIcons.iconFor(poi("amenity:fountain")))
        assertSame(Icons.Filled.Place, PlaceIcons.iconFor(poi(null)))
    }

    @Test
    fun `les villes ont leur icône`() {
        assertSame(HudIcons.City, PlaceIcons.iconFor(poi(null).copy(kind = PlaceKind.CITY)))
    }
}
