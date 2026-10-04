package com.gamemaps.irl.ui.search

import com.gamemaps.irl.data.search.Place

/**
 * Résultats de la recherche. Le texte tapé, lui, est gardé par l'écran (voir MainScreen) :
 * le faire transiter par le ViewModel, de façon asynchrone, mélange les lettres quand on tape vite.
 */
data class SearchUiState(
    val results: List<Place> = emptyList(),
    val isLoading: Boolean = false,
)
