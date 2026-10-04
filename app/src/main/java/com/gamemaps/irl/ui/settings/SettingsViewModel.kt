package com.gamemaps.irl.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.places.SavedPlacesRepository
import com.gamemaps.irl.data.routing.tomtom.TomTomKeyFormat
import com.gamemaps.irl.data.routing.tomtom.TomTomKeyStore
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceSearch
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.data.settings.AudioPreferences
import com.gamemaps.irl.data.settings.SettingsRepository
import com.gamemaps.irl.di.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Logique du menu Paramètres : réglages, son, clé TomTom, lieux enregistrés (dont le choix de Maison et Travail). */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val audioPreferences: AudioPreferences,
    private val tomTomKeyStore: TomTomKeyStore,
    private val placeSearch: PlaceSearch,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val placeResults = MutableStateFlow<List<Place>>(emptyList())
    private var placeSearchJob: Job? = null

    /** Réglages, lieux, son et clé : ce qui vient des dépôts. */
    private val stored = combine(
        settingsRepository.settings,
        savedPlacesRepository.saved,
        audioPreferences.muted,
        tomTomKeyStore.key,
    ) { settings, saved, muted, key -> SettingsUiState(settings, saved, muted, key?.let(TomTomKeyFormat::mask)) }

    val uiState: StateFlow<SettingsUiState> = combine(stored, placeResults, locationRepository.fixes) { state, results, fix ->
        state.copy(placeResults = results, hasPosition = fix != null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun update(change: (AppSettings) -> AppSettings) = settingsRepository.update(change)

    fun toggleMuted() = audioPreferences.toggleMuted()

    /** @return false si la clé collée n'a pas la forme d'une clé TomTom. */
    fun saveTomTomKey(raw: String): Boolean = tomTomKeyStore.save(raw)

    fun clearTomTomKey() = tomTomKeyStore.clear()

    /** Recherche d'adresse pour Maison / Travail : attend la fin de la frappe, annule la requête précédente. */
    fun searchPlace(query: String) {
        placeSearchJob?.cancel()
        if (query.isBlank()) {
            placeResults.value = emptyList()
            return
        }
        placeSearchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            placeResults.value = try {
                placeSearch.search(query, locationRepository.fixes.value?.position)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun setHome(place: Place) {
        savedPlacesRepository.setHome(place)
        searchPlace("")
    }

    fun setWork(place: Place) {
        savedPlacesRepository.setWork(place)
        searchPlace("")
    }

    fun addFavorite(place: Place) {
        savedPlacesRepository.addFavorite(place)
        searchPlace("")
    }

    /** L'endroit où se trouve le téléphone, à enregistrer comme Maison, Travail ou favori ; null sans position GPS. */
    fun currentPositionAsPlace(): Place? = locationRepository.fixes.value?.let { fix ->
        Place(id = "here-${fix.position.lat}-${fix.position.lng}", name = "Position enregistrée", subtitle = "", position = fix.position)
    }

    fun clearHome() = savedPlacesRepository.clearHome()

    fun clearWork() = savedPlacesRepository.clearWork()

    fun removeFavorite(place: Place) = savedPlacesRepository.removeFavorite(place)

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 350L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    savedPlacesRepository = container.savedPlacesRepository,
                    audioPreferences = container.audioPreferences,
                    tomTomKeyStore = container.tomTomKeyStore,
                    placeSearch = container.placeSearch,
                    locationRepository = container.locationRepository,
                )
            }
        }
    }
}
