package com.gamemaps.irl.data.places

import com.gamemaps.irl.data.search.Place

/** Lieux enregistrés par l'utilisateur (cahier des charges §8). */
data class SavedPlaces(
    val home: Place? = null,
    val work: Place? = null,
    val favorites: List<Place> = emptyList(),
) {
    fun isFavorite(place: Place): Boolean = favorites.any { it.id == place.id }

    val isEmpty: Boolean get() = home == null && work == null && favorites.isEmpty()
}
