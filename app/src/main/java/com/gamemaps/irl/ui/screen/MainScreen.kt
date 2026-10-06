package com.gamemaps.irl.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.ui.hud.GpsStatusDot
import com.gamemaps.irl.ui.hud.ManeuverBanner
import com.gamemaps.irl.ui.hud.MissionPassedOverlay
import com.gamemaps.irl.navigation.trip.toMissionPassedModel
import com.gamemaps.irl.ui.hud.MuteButton
import com.gamemaps.irl.ui.hud.PreviewPanel
import com.gamemaps.irl.ui.hud.RecenterButton
import com.gamemaps.irl.ui.hud.toPreviewModel
import com.gamemaps.irl.ui.hud.RadarAlertBanner
import com.gamemaps.irl.ui.hud.SpeedGauge
import com.gamemaps.irl.ui.hud.TripBar
import com.gamemaps.irl.ui.hud.StatusBanner
import com.gamemaps.irl.ui.hud.toHudModel
import com.gamemaps.irl.ui.map.MapViewHost
import com.gamemaps.irl.ui.permissions.LocationPermissionEffect
import com.gamemaps.irl.ui.search.SavedPlacesShortcuts
import com.gamemaps.irl.ui.settings.SettingsScreen
import com.gamemaps.irl.ui.settings.SettingsSection
import com.gamemaps.irl.ui.settings.SettingsViewModel
import com.gamemaps.irl.ui.search.SearchBar
import com.gamemaps.irl.ui.search.SearchResultsList

/**
 * Écran principal du téléphone : carte plein écran + HUD superposé (cahier des charges §2.2).
 *
 *  ┌ barre de recherche / guidage ┐
 *  │            carte             │
 *  └ vitesse            arrivée   ┘
 */
@Composable
fun MainScreen(viewModel: MainViewModel, settingsViewModel: SettingsViewModel, onLocationPermissionGranted: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mapController by remember { mutableStateOf<MapController?>(null) }
    // Écran des Paramètres à ouvrir, ou null quand le menu est fermé.
    var settingsStart by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    val followingFlow = remember(mapController) { mapController?.isFollowing ?: MutableStateFlow(true) }
    val isFollowing by followingFlow.collectAsStateWithLifecycle()

    KeepScreenOnEffect()
    LocationPermissionEffect(onGranted = onLocationPermissionGranted)
    MapRenderEffect(mapController, state)

    Box(Modifier.fillMaxSize()) {
        MapViewHost(theme = state.settings.theme, modifier = Modifier.fillMaxSize()) { mapController = it }

        TopArea(
            state = state,
            viewModel = viewModel,
            onOpenSettings = { settingsStart = SettingsSection.ROOT },
            onDefinePlaces = { settingsStart = SettingsSection.PLACES },
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp),
        )
        BottomArea(
            state = state,
            isFollowing = isFollowing,
            actions = BottomActions(
                onStop = viewModel::onStopNavigation,
                onToggleMute = viewModel::onToggleMute,
                onConfirmRoute = viewModel::onConfirmRoute,
                onSelectRoute = viewModel::onSelectRoute,
                onRecenter = { mapController?.recenter() },
                onToggleFavorite = viewModel::onToggleFavorite,
            ),
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(12.dp),
        )
        (state.navigation as? NavigationState.Arrived)?.let { arrived ->
            MissionPassedOverlay(arrived.toMissionPassedModel(), onDismiss = viewModel::onStopNavigation)
        }
        settingsStart?.let { start -> SettingsScreen(settingsViewModel, startSection = start, onClose = { settingsStart = null }) }
    }
}

@Composable
private fun TopArea(
    state: MainUiState,
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
    onDefinePlaces: () -> Unit,
    modifier: Modifier,
) {
    // Texte du champ gardé localement : mis à jour immédiatement à chaque frappe.
    var query by rememberSaveable { mutableStateOf("") }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (val navigation = state.navigation) {
            is NavigationState.Idle -> {
                SearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        viewModel.onQueryChange(it)
                    },
                    onMenuClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    trailing = { GpsStatusDot(state.driving.gpsQuality) },
                )
                if (query.isBlank()) {
                    SavedPlacesShortcuts(saved = state.savedPlaces, onSelect = viewModel::onPlaceSelected, onDefine = onDefinePlaces)
                }
                if (state.search.results.isNotEmpty()) {
                    SearchResultsList(
                        results = state.search.results,
                        near = state.driving.fix?.position,
                        onSelect = { place ->
                            query = ""
                            viewModel.onPlaceSelected(place)
                        },
                    )
                }
            }
            is NavigationState.Previewing -> Unit // L'aperçu s'affiche en bas (PreviewPanel).
            is NavigationState.Calculating ->
                StatusBanner("Calcul de l'itinéraire vers ${navigation.destination.name}…", loading = true, onDismiss = viewModel::onStopNavigation)
            is NavigationState.Navigating ->
                ManeuverBanner(navigation.toHudModel())
            is NavigationState.Arrived -> Unit // Écran plein "Mission accomplie" (voir MainScreen).
            is NavigationState.Failed ->
                StatusBanner(navigation.message, onDismiss = viewModel::onStopNavigation)
        }
        state.driving.radarAlert?.let { RadarAlertBanner(it) }
    }
}

@Composable
private fun BottomArea(state: MainUiState, isFollowing: Boolean, actions: BottomActions, modifier: Modifier) {
    val navigation = state.navigation
    if (navigation is NavigationState.Previewing) {
        val destination = navigation.destination
        PreviewPanel(
            preview = navigation.toPreviewModel(),
            isFavorite = state.savedPlaces.isFavorite(destination),
            onToggleFavorite = { actions.onToggleFavorite(destination) },
            onSelectRoute = actions.onSelectRoute,
            onStart = actions.onConfirmRoute,
            onCancel = actions.onStop,
            modifier = modifier,
        )
        return
    }
    // Compteur à gauche, boutons ronds à droite ; pendant le guidage, la barre de trajet en dessous.
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            SpeedGauge(speedKmh = state.driving.fix?.speedKmh ?: 0, limit = state.driving.speedLimit)
            Box(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isFollowing) RecenterButton(onClick = actions.onRecenter)
                // Pendant le guidage, le bouton du son est dans la barre de trajet.
                if (navigation !is NavigationState.Navigating) MuteButton(isMuted = state.isMuted, onToggle = actions.onToggleMute)
            }
        }
        if (navigation is NavigationState.Navigating) {
            TripBar(hud = navigation.toHudModel(), isMuted = state.isMuted, onStop = actions.onStop, onToggleMute = actions.onToggleMute)
        }
    }
}

/** Actions des boutons du bas de l'écran, regroupées pour garder des signatures lisibles. */
private class BottomActions(
    val onStop: () -> Unit,
    val onToggleMute: () -> Unit,
    val onConfirmRoute: () -> Unit,
    val onSelectRoute: (Int) -> Unit,
    val onRecenter: () -> Unit,
    val onToggleFavorite: (Place) -> Unit,
)
