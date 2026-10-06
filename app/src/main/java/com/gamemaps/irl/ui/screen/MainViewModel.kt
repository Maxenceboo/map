package com.gamemaps.irl.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.places.SavedPlacesRepository
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceSearch
import com.gamemaps.irl.data.settings.AudioPreferences
import com.gamemaps.irl.data.settings.SettingsRepository
import com.gamemaps.irl.navigation.radar.RadarAlert
import kotlinx.coroutines.flow.Flow
import com.gamemaps.irl.data.radar.RadarRepository
import com.gamemaps.irl.data.speedlimit.SpeedLimitRepository
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
    private val displayFixes: Flow<GpsFix?>,
    private val speedLimitRepository: SpeedLimitRepository,
    private val radarRepository: RadarRepository,
    private val radarAlerts: Flow<RadarAlert?>,
    private val audioPreferences: AudioPreferences,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val search = MutableStateFlow(SearchUiState())
    private val queries = MutableStateFlow("")

    private val driving = combine(
        displayFixes,
        speedLimitRepository.limit,
        radarRepository.radars,
        radarAlerts,
    ) { fix, speedLimit, radars, radarAlert ->
        DrivingState(fix = fix, speedLimit = speedLimit, radars = radars, radarAlert = radarAlert)
    }

    private val preferences = combine(audioPreferences.muted, savedPlacesRepository.saved, settingsRepository.settings, ::Triple)

    val uiState: StateFlow<MainUiState> = combine(
        search,
        navigationEngine.state,
        driving,
        preferences,
    ) { searchState, navigation, drivingState, (muted, saved, settings) ->
        MainUiState(
            search = searchState,
            navigation = navigation,
            driving = drivingState,
            isMuted = muted,
            savedPlaces = saved,
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        observeQueries()
    }

    fun onQueryChange(query: String) {
        queries.value = query
    }

    fun onPlaceSelected(place: Place) {
        search.value = SearchUiState()
        queries.value = ""
        navigationEngine.start(place)
    }

    fun onSelectRoute(index: Int) = navigationEngine.selectRoute(index)

    fun onConfirmRoute() {
        navigationEngine.confirm()
    }

    fun onStopNavigation() {
        navigationEngine.stop()
    }

    fun onToggleMute() {
        audioPreferences.toggleMuted()
    }

    fun onToggleFavorite(place: Place) = savedPlacesRepository.toggleFavorite(place)

    /** Attend 350 ms sans frappe avant d'interroger le géocodeur ; annule la requête précédente. */
    @OptIn(FlowPreview::class)
    private fun observeQueries() {
        viewModelScope.launch {
            queries.debounce(DEBOUNCE_MS).distinctUntilChanged().collectLatest { query ->
                if (query.isBlank()) {
                    search.value = SearchUiState()
                    return@collectLatest
                }
                search.update { it.copy(isLoading = true) }
                val results = try {
                    placeSearch.search(query, locationRepository.fixes.value?.position)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null // réseau absent ou serveur injoignable
                }
                val message = when {
                    results == null -> "Recherche impossible. Vérifiez votre connexion."
                    results.isEmpty() -> "Aucun résultat pour « ${query.trim()} »"
                    else -> null
                }
                search.value = SearchUiState(results = results.orEmpty(), message = message)
            }
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 350L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    placeSearch = container.placeSearch,
                    navigationEngine = container.navigationEngine,
                    locationRepository = container.locationRepository,
                    displayFixes = container.displayFixes,
                    speedLimitRepository = container.speedLimitRepository,
                    radarRepository = container.radarRepository,
                    radarAlerts = container.radarAlerts,
                    settingsRepository = container.settingsRepository,
                    audioPreferences = container.audioPreferences,
                    savedPlacesRepository = container.savedPlacesRepository,
                )
            }
        }
    }
}
