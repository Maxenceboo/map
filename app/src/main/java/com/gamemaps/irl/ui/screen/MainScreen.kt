package com.gamemaps.irl.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.ui.hud.ArrivalPanel
import com.gamemaps.irl.ui.hud.GpsStatusDot
import com.gamemaps.irl.ui.hud.ManeuverBanner
import com.gamemaps.irl.ui.hud.MuteButton
import com.gamemaps.irl.ui.hud.PreviewPanel
import com.gamemaps.irl.ui.hud.RecenterButton
import com.gamemaps.irl.ui.hud.toPreviewModel
import com.gamemaps.irl.ui.hud.RadarAlertBanner
import com.gamemaps.irl.ui.hud.SpeedPanel
import com.gamemaps.irl.ui.hud.StatusBanner
import com.gamemaps.irl.ui.hud.toHudModel
import com.gamemaps.irl.ui.map.MapViewHost
import com.gamemaps.irl.ui.permissions.LocationPermissionEffect
import com.gamemaps.irl.ui.search.SearchBar
import com.gamemaps.irl.ui.search.SearchResultsList
import com.gamemaps.irl.ui.theme.CockpitColors

/**
 * Écran principal du téléphone : carte plein écran + HUD superposé (cahier des charges §2.2).
 *
 *  ┌ barre de recherche / guidage ┐
 *  │            carte             │
 *  └ vitesse            arrivée   ┘
 */
@Composable
fun MainScreen(viewModel: MainViewModel, onLocationPermissionGranted: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mapController by remember { mutableStateOf<MapController?>(null) }
    val followingFlow = remember(mapController) { mapController?.isFollowing ?: MutableStateFlow(true) }
    val isFollowing by followingFlow.collectAsStateWithLifecycle()

    KeepScreenOnEffect()
    LocationPermissionEffect(onGranted = onLocationPermissionGranted)
    MapRenderEffect(mapController, state)

    Box(Modifier.fillMaxSize()) {
        MapViewHost(theme = MapTheme.GTA_RADAR, modifier = Modifier.fillMaxSize()) { mapController = it }

        TopArea(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp),
        )
        BottomArea(
            state = state,
            isFollowing = isFollowing,
            actions = BottomActions(
                onStop = viewModel::onStopNavigation,
                onToggleMute = viewModel::onToggleMute,
                onConfirmRoute = viewModel::onConfirmRoute,
                onRecenter = { mapController?.recenter() },
            ),
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(12.dp),
        )
    }
}

@Composable
private fun TopArea(state: MainUiState, viewModel: MainViewModel, modifier: Modifier) {
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
                    modifier = Modifier.fillMaxWidth(),
                    trailing = { GpsStatusDot(state.driving.gpsQuality) },
                )
                if (state.search.results.isNotEmpty()) {
                    SearchResultsList(
                        results = state.search.results,
                        onSelect = { place ->
                            query = ""
                            viewModel.onPlaceSelected(place)
                        },
                    )
                }
            }
            is NavigationState.Previewing -> Unit // L'aperçu s'affiche en bas (PreviewPanel).
            is NavigationState.Calculating ->
                StatusBanner("Calcul de l'itinéraire vers ${navigation.destination.name}…")
            is NavigationState.Navigating ->
                ManeuverBanner(navigation.toHudModel())
            is NavigationState.Arrived ->
                StatusBanner("Mission accomplie : ${navigation.destination.name}", color = CockpitColors.Route, onDismiss = viewModel::onStopNavigation)
            is NavigationState.Failed ->
                StatusBanner("Échec : ${navigation.message}", color = CockpitColors.Danger, onDismiss = viewModel::onStopNavigation)
        }
        state.driving.radarAlert?.let { RadarAlertBanner(it) }
    }
}

@Composable
private fun BottomArea(state: MainUiState, isFollowing: Boolean, actions: BottomActions, modifier: Modifier) {
    val navigation = state.navigation
    if (navigation is NavigationState.Previewing) {
        PreviewPanel(
            preview = navigation.toPreviewModel(),
            onStart = actions.onConfirmRoute,
            onCancel = actions.onStop,
            modifier = modifier,
        )
        return
    }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isFollowing) {
            RecenterButton(onClick = actions.onRecenter)
            Spacer(Modifier.height(12.dp))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            SpeedPanel(speedKmh = state.driving.fix?.speedKmh ?: 0, limitKmh = state.driving.speedLimitKmh)
            Box(Modifier.weight(1f))
            MuteButton(isMuted = state.isMuted, onToggle = actions.onToggleMute)
            if (navigation is NavigationState.Navigating) {
                Spacer(Modifier.width(8.dp))
                ArrivalPanel(hud = navigation.toHudModel(), onStop = actions.onStop)
            }
        }
    }
}

/** Actions des boutons du bas de l'écran, regroupées pour garder des signatures lisibles. */
private class BottomActions(
    val onStop: () -> Unit,
    val onToggleMute: () -> Unit,
    val onConfirmRoute: () -> Unit,
    val onRecenter: () -> Unit,
)
