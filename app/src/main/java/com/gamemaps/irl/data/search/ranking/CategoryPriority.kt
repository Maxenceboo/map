package com.gamemaps.irl.data.search.ranking

import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind

/**
 * Importance d'un lieu comme destination (0 = peu utile, 3 = très utile).
 * Ex. pour "Gare Saint-Jean" : la gare (3) passe avant l'arrêt de tram du même nom (0).
 */
object CategoryPriority {

    private val MAJOR = setOf(
        "railway:station", "building:train_station", "public_transport:station", "railway:halt",
        "aeroway:aerodrome", "amenity:hospital", "amenity:university",
    )
    private val MINOR = setOf("highway:bus_stop", "railway:tram_stop", "public_transport:platform", "public_transport:stop_position")
    private val USEFUL_PREFIXES = listOf(
        "shop:", "tourism:", "leisure:", "amenity:fuel", "amenity:parking", "amenity:restaurant", "amenity:bus_station",
    )

    fun of(place: Place): Int {
        if (place.kind != PlaceKind.POI) return 2
        val category = place.category ?: return 1
        return when {
            category in MAJOR -> 3
            category in MINOR -> 0
            USEFUL_PREFIXES.any { category.startsWith(it) } -> 2
            else -> 1
        }
    }
}
