package com.gamemaps.irl.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gamemaps.irl.data.places.SavedPlacesRepository
import com.gamemaps.irl.data.routing.tomtom.TomTomKeyFormat
import com.gamemaps.irl.data.routing.tomtom.TomTomKeyStore
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.data.settings.AudioPreferences
import com.gamemaps.irl.data.settings.SettingsRepository
import com.gamemaps.irl.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Logique du menu Paramètres : lit et modifie réglages, son, lieux enregistrés et clé TomTom. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val audioPreferences: AudioPreferences,
    private val tomTomKeyStore: TomTomKeyStore,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        savedPlacesRepository.saved,
        audioPreferences.muted,
        tomTomKeyStore.key,
    ) { settings, saved, muted, key -> SettingsUiState(settings, saved, muted, key?.let(TomTomKeyFormat::mask)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun update(change: (AppSettings) -> AppSettings) = settingsRepository.update(change)

    fun toggleMuted() = audioPreferences.toggleMuted()

    /** @return false si la clé collée n'a pas la forme d'une clé TomTom. */
    fun saveTomTomKey(raw: String): Boolean = tomTomKeyStore.save(raw)

    fun clearTomTomKey() = tomTomKeyStore.clear()

    fun clearHome() = savedPlacesRepository.clearHome()

    fun clearWork() = savedPlacesRepository.clearWork()

    fun removeFavorite(place: Place) = savedPlacesRepository.removeFavorite(place)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(container.settingsRepository, container.savedPlacesRepository, container.audioPreferences, container.tomTomKeyStore)
            }
        }
    }
}
