package com.gamemaps.irl.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceSearch
import com.gamemaps.irl.di.AppContainer
import com.gamemaps.irl.navigation.NavigationEngine
import com.gamemaps.irl.ui.search.SearchUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Logique de l'écran téléphone : recherche avec anti-rebond, lancement et arrêt du guidage.
 * Le guidage lui-même vit dans [NavigationEngine], partagé avec Android Auto.
 */
class MainViewModel(
    private val placeSearch: PlaceSearch,
    private val navigationEngine: NavigationEngine,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val search = MutableStateFlow(SearchUiState())
    private val queries = MutableStateFlow("")

    val uiState: StateFlow<MainUiState> = combine(
        search,
        navigationEngine.state,
        locationRepository.fixes,
    ) { searchState, navigation, fix ->
        MainUiState(search = searchState, navigation = navigation, fix = fix)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        observeQueries()
    }

    fun onQueryChange(query: String) {
        search.update { it.copy(query = query) }
        queries.value = query
    }

    fun onPlaceSelected(place: Place) {
        search.value = SearchUiState()
        queries.value = ""
        navigationEngine.start(place)
    }

    fun onStopNavigation() {
        navigationEngine.stop()
    }

    /** Attend 350 ms sans frappe avant d'interroger le géocodeur ; annule la requête précédente. */
    @OptIn(FlowPreview::class)
    private fun observeQueries() {
        viewModelScope.launch {
            queries.debounce(DEBOUNCE_MS).distinctUntilChanged().collectLatest { query ->
                if (query.isBlank()) {
                    search.update { it.copy(results = emptyList(), isLoading = false) }
                    return@collectLatest
                }
                search.update { it.copy(isLoading = true) }
                val results = try {
                    placeSearch.search(query, locationRepository.fixes.value?.position)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emptyList()
                }
                search.update { it.copy(results = results, isLoading = false) }
            }
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 350L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(container.placeSearch, container.navigationEngine, container.locationRepository)
            }
        }
    }
}
