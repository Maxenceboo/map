package com.gamemaps.irl.ui.search

import com.gamemaps.irl.data.search.Place

/** État de la barre de recherche. */
data class SearchUiState(
    val query: String = "",
    val results: List<Place> = emptyList(),
    val isLoading: Boolean = false,
)
