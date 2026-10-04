package com.gamemaps.irl.data.places

import android.content.Context
import com.gamemaps.irl.data.search.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Maison, Travail et favoris, sauvegardés sur le téléphone (aucun compte, aucun cloud).
 * Partagé entre le téléphone et Android Auto.
 */
class SavedPlacesRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _saved = MutableStateFlow(SavedPlacesSerializer.fromJson(prefs.getString(KEY, null)))
    val saved: StateFlow<SavedPlaces> = _saved.asStateFlow()

    fun setHome(place: Place) = update { it.copy(home = place) }

    fun setWork(place: Place) = update { it.copy(work = place) }

    fun clearHome() = update { it.copy(home = null) }

    fun clearWork() = update { it.copy(work = null) }

    fun removeFavorite(place: Place) = update { saved -> saved.copy(favorites = saved.favorites.filterNot { it.id == place.id }) }

    /** Ajoute le lieu aux favoris, ou l'en retire s'il y est déjà. */
    fun toggleFavorite(place: Place) = update { saved ->
        if (saved.isFavorite(place)) {
            saved.copy(favorites = saved.favorites.filterNot { it.id == place.id })
        } else {
            saved.copy(favorites = listOf(place) + saved.favorites)
        }
    }

    private fun update(change: (SavedPlaces) -> SavedPlaces) {
        val newValue = change(_saved.value)
        prefs.edit().putString(KEY, SavedPlacesSerializer.toJson(newValue)).apply()
        _saved.value = newValue
    }

    private companion object {
        const val FILE_NAME = "saved_places"
        const val KEY = "places_v1"
    }
}
