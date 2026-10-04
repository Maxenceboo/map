package com.gamemaps.irl.ui.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind
import com.gamemaps.irl.ui.icons.HudIcons

/**
 * Icône d'un résultat de recherche, d'après sa nature et sa catégorie OpenStreetMap
 * ("railway:station", "amenity:fuel", "shop:supermarket"…). À défaut : le repère de lieu.
 */
object PlaceIcons {

    fun iconFor(place: Place): ImageVector = when (place.kind) {
        PlaceKind.CITY -> HudIcons.City
        PlaceKind.ADDRESS -> Icons.Filled.Home
        PlaceKind.STREET -> HudIcons.Navigation
        PlaceKind.POI -> forCategory(place.category)
    }

    private fun forCategory(category: String?): ImageVector {
        val key = category?.substringBefore(':')
        val value = category?.substringAfter(':', "")
        return when {
            key == "railway" || value in TRANSIT -> HudIcons.Train
            value in FUEL -> HudIcons.Fuel
            value == "parking" -> HudIcons.Parking
            value in FOOD -> HudIcons.Restaurant
            key == "tourism" && value in LODGING -> HudIcons.Hotel
            key == "shop" -> Icons.Filled.ShoppingCart
            else -> Icons.Filled.Place
        }
    }

    private val TRANSIT = setOf("bus_station", "station", "ferry_terminal")
    private val FUEL = setOf("fuel", "charging_station")
    private val FOOD = setOf("restaurant", "cafe", "fast_food", "bar", "pub", "food_court")
    private val LODGING = setOf("hotel", "motel", "hostel", "guest_house", "camp_site")
}
