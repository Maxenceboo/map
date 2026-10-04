package com.gamemaps.irl.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamemaps.irl.ui.settings.components.SettingsHeader
import com.gamemaps.irl.ui.settings.sections.AboutSection
import com.gamemaps.irl.ui.settings.sections.AudioSection
import com.gamemaps.irl.ui.settings.sections.PerspectiveSection
import com.gamemaps.irl.ui.settings.sections.PlacePickerSection
import com.gamemaps.irl.ui.settings.sections.PlacesSection
import com.gamemaps.irl.ui.settings.sections.RadarSection
import com.gamemaps.irl.ui.settings.sections.RootSection
import com.gamemaps.irl.ui.settings.sections.ThemeSection
import com.gamemaps.irl.ui.settings.sections.TrafficSettingsSection
import com.gamemaps.irl.ui.settings.sections.VehicleColorSection
import com.gamemaps.irl.ui.settings.sections.VehicleModelSection
import com.gamemaps.irl.ui.settings.sections.VehicleSection
import com.gamemaps.irl.ui.theme.CockpitColors

/**
 * Menu Paramètres plein écran, par-dessus la carte.
 * Navigation simple : la section affichée est un état ; "Retour" (ou le bouton retour du téléphone)
 * remonte d'un niveau, puis ferme le menu. [startSection] ouvre directement un écran précis.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onClose: () -> Unit, startSection: SettingsSection = SettingsSection.ROOT) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf(startSection) }
    // Maison ou Travail choisi : on efface la recherche et on revient à la liste des lieux.
    val backToPlaces = {
        viewModel.searchPlace("")
        section = SettingsSection.PLACES
    }
    val goBack = { section.parent?.let { section = it } ?: onClose() }

    BackHandler(onBack = goBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(CockpitColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        SettingsHeader(
            title = section.title,
            backLabel = section.parent?.title ?: "Carte",
            onBack = goBack,
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (section) {
                SettingsSection.ROOT -> RootSection(state) { section = it }
                SettingsSection.THEME -> ThemeSection(state.settings.theme) { theme -> viewModel.update { it.copy(theme = theme) } }
                SettingsSection.PERSPECTIVE -> PerspectiveSection(state.settings.perspective) { p -> viewModel.update { it.copy(perspective = p) } }
                SettingsSection.VEHICLE -> VehicleSection(state.settings, open = { section = it }, onUpdate = viewModel::update)
                SettingsSection.VEHICLE_MODEL -> VehicleModelSection(state.settings.vehicle) { kind -> viewModel.update { it.copy(vehicle = kind) } }
                SettingsSection.VEHICLE_COLOR -> VehicleColorSection(state.settings.vehicleColor) { color -> viewModel.update { it.copy(vehicleColor = color) } }
                SettingsSection.AUDIO -> AudioSection(state.settings, state.isMuted, viewModel::toggleMuted, viewModel::update)
                SettingsSection.RADARS -> RadarSection(state.settings, viewModel::update)
                SettingsSection.TRAFFIC -> TrafficSettingsSection(state.settings, state.maskedTomTomKey, viewModel::update, viewModel::saveTomTomKey, viewModel::clearTomTomKey)
                SettingsSection.PLACES -> PlacesSection(state.savedPlaces, open = { section = it }, onRemoveFavorite = viewModel::removeFavorite)
                SettingsSection.PLACE_HOME -> PlacePickerSection(
                    current = state.savedPlaces.home,
                    results = state.placeResults,
                    onSearch = viewModel::searchPlace,
                    onPick = { viewModel.setHome(it); backToPlaces() },
                    onUseCurrentPosition = { viewModel.currentPositionAsPlace()?.let(viewModel::setHome); backToPlaces() }.takeIf { state.hasPosition },
                    onClear = viewModel::clearHome,
                )
                SettingsSection.PLACE_WORK -> PlacePickerSection(
                    current = state.savedPlaces.work,
                    results = state.placeResults,
                    onSearch = viewModel::searchPlace,
                    onPick = { viewModel.setWork(it); backToPlaces() },
                    onUseCurrentPosition = { viewModel.currentPositionAsPlace()?.let(viewModel::setWork); backToPlaces() }.takeIf { state.hasPosition },
                    onClear = viewModel::clearWork,
                )
                SettingsSection.ABOUT -> AboutSection()
            }
        }
    }
}
