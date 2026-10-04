package com.gamemaps.irl.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gamemaps.irl.data.custom.CustomContentRepository
import com.gamemaps.irl.data.custom.CustomThemeSpec
import com.gamemaps.irl.data.custom.CustomVehicleSpec
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

/** Logique du menu Paramètres : réglages, son, clé TomTom, lieux enregistrés, thèmes et véhicules créés en mode développeur. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val audioPreferences: AudioPreferences,
    private val tomTomKeyStore: TomTomKeyStore,
    private val placeSearch: PlaceSearch,
    private val locationRepository: LocationRepository,
    private val customContent: CustomContentRepository,
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

    val uiState: StateFlow<SettingsUiState> = combine(
        stored,
        placeResults,
        locationRepository.fixes,
        customContent.themes,
        customContent.vehicles,
    ) { state, results, fix, themes, vehicles ->
        state.copy(placeResults = results, hasPosition = fix != null, customThemes = themes, customVehicles = vehicles)
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

    // ---- Mode développeur : thèmes et véhicules personnalisés ----

    /** Crée un thème à partir des couleurs du thème actuel ; renvoie son identifiant pour l'ouvrir dans l'éditeur. */
    fun createTheme(): String {
        val spec = CustomThemeSpec(
            id = "custom-theme-${System.currentTimeMillis()}",
            name = "Mon thème ${customContent.themes.value.size + 1}",
            palette = settingsRepository.settings.value.theme.palette,
        )
        customContent.saveTheme(spec)
        return spec.id
    }

    /** Enregistre le thème ; s'il est celui affiché, la carte est repeinte tout de suite. */
    fun saveTheme(spec: CustomThemeSpec) {
        customContent.saveTheme(spec)
        if (settingsRepository.settings.value.theme.id == spec.id) useTheme(spec)
    }

    fun useTheme(spec: CustomThemeSpec) = settingsRepository.update { it.copy(theme = spec.toMapTheme()) }

    /** Supprime le thème ; s'il était affiché, on revient au thème par défaut. */
    fun deleteTheme(id: String) {
        customContent.deleteTheme(id)
        if (settingsRepository.settings.value.theme.id == id) settingsRepository.update { it.copy(theme = AppSettings().theme) }
    }

    /** Crée un véhicule aux mesures par défaut ; renvoie son identifiant pour l'ouvrir dans l'éditeur. */
    fun createVehicle(): String {
        val spec = CustomVehicleSpec(
            id = "custom-vehicle-${System.currentTimeMillis()}",
            name = "Mon véhicule ${customContent.vehicles.value.size + 1}",
        )
        customContent.saveVehicle(spec)
        return spec.id
    }

    /** Enregistre le véhicule ; s'il est celui affiché, il est redessiné tout de suite. */
    fun saveVehicle(spec: CustomVehicleSpec) {
        customContent.saveVehicle(spec)
        if (settingsRepository.settings.value.vehicle.id == spec.id) useVehicle(spec)
    }

    fun useVehicle(spec: CustomVehicleSpec) = settingsRepository.update { it.copy(vehicle = spec.toVehicleKind()) }

    /** Supprime le véhicule ; s'il était affiché, on revient au véhicule par défaut. */
    fun deleteVehicle(id: String) {
        customContent.deleteVehicle(id)
        if (settingsRepository.settings.value.vehicle.id == id) settingsRepository.update { it.copy(vehicle = AppSettings().vehicle) }
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
                    customContent = container.customContentRepository,
                )
            }
        }
    }
}
