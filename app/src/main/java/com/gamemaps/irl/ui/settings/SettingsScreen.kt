package com.gamemaps.irl.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamemaps.irl.ui.settings.components.SettingsHeader
import com.gamemaps.irl.ui.settings.sections.AboutSection
import com.gamemaps.irl.ui.settings.sections.AudioSection
import com.gamemaps.irl.ui.settings.sections.PerspectiveSection
import com.gamemaps.irl.ui.settings.sections.PlacesSection
import com.gamemaps.irl.ui.settings.sections.RadarSection
import com.gamemaps.irl.ui.settings.sections.RootSection
import com.gamemaps.irl.ui.settings.sections.ThemeSection
import com.gamemaps.irl.ui.theme.CockpitColors

/**
 * Menu Paramètres plein écran, par-dessus la carte.
 * Navigation simple : la section affichée est un état ; "Retour" (ou le bouton retour du téléphone)
 * remonte d'un niveau, puis ferme le menu.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onClose: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf(SettingsSection.ROOT) }
    val goBack = { if (section == SettingsSection.ROOT) onClose() else section = SettingsSection.ROOT }

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
            backLabel = if (section == SettingsSection.ROOT) "Carte" else "Paramètres",
            onBack = goBack,
        )
        Column(Modifier.verticalScroll(rememberScrollState())) {
            when (section) {
                SettingsSection.ROOT -> RootSection(state) { section = it }
                SettingsSection.THEME -> ThemeSection(state.settings.theme) { theme -> viewModel.update { it.copy(theme = theme) } }
                SettingsSection.PERSPECTIVE -> PerspectiveSection(state.settings.perspective) { p -> viewModel.update { it.copy(perspective = p) } }
                SettingsSection.AUDIO -> AudioSection(state.settings, state.isMuted, viewModel::toggleMuted, viewModel::update)
                SettingsSection.RADARS -> RadarSection(state.settings, viewModel::update)
                SettingsSection.PLACES -> PlacesSection(state.savedPlaces, viewModel::clearHome, viewModel::clearWork, viewModel::removeFavorite)
                SettingsSection.ABOUT -> AboutSection()
            }
        }
    }
}
