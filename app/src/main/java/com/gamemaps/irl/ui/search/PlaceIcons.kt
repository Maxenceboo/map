package com.gamemaps.irl.ui.search

import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind

/** Pictogramme d'un lieu selon sa nature et, pour un point d'intérêt, sa catégorie OSM. */
object PlaceIcons {

    private val BY_CATEGORY = mapOf(
        "railway:station" to "🚉",
        "building:train_station" to "🚉",
        "public_transport:station" to "🚉",
        "amenity:bus_station" to "🚌",
        "railway:tram_stop" to "🚊",
        "highway:bus_stop" to "🚏",
        "aeroway:aerodrome" to "✈",
        "amenity:fuel" to "⛽",
        "amenity:charging_station" to "🔌",
        "amenity:parking" to "🅿",
        "amenity:hospital" to "🏥",
        "amenity:pharmacy" to "💊",
        "amenity:restaurant" to "🍴",
        "amenity:fast_food" to "🍔",
        "amenity:cafe" to "☕",
        "amenity:school" to "🏫",
        "amenity:university" to "🎓",
        "tourism:hotel" to "🏨",
        "leisure:stadium" to "🏟",
        "leisure:park" to "🌳",
    )

    fun glyph(place: Place): String = when (place.kind) {
        PlaceKind.ADDRESS -> "🏠"
        PlaceKind.STREET -> "🛣"
        PlaceKind.CITY -> "🏙"
        PlaceKind.POI -> place.category?.let { category ->
            BY_CATEGORY[category] ?: if (category.startsWith("shop:")) "🛒" else null
        } ?: "📍"
    }
}
