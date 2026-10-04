package com.gamemaps.irl.ui.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.map.vehicle3d.VehicleColor
import com.gamemaps.irl.map.vehicle3d.VehicleKind
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.SettingsSection
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow
import com.gamemaps.irl.ui.settings.components.SettingsOptionRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow
import com.gamemaps.irl.ui.theme.CockpitColors

/** Paramètres > Véhicule : deux sous-menus (modèle, couleur) et l'interrupteur des phares. */
@Composable
fun VehicleSection(settings: AppSettings, open: (SettingsSection) -> Unit, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsGroup {
        SettingsNavigationRow(HudIcons.Car, "Modèle", settings.vehicle.label) { open(SettingsSection.VEHICLE_MODEL) }
        SettingsNavigationRow(HudIcons.Palette, "Couleur", settings.vehicleColor.label) { open(SettingsSection.VEHICLE_COLOR) }
    }
    SettingsGroup {
        SettingsToggleRow(HudIcons.Bolt, "Phares", "Faisceaux projetés sur la route devant le véhicule 3D", settings.headlights) { enabled ->
            onUpdate { it.copy(headlights = enabled) }
        }
    }
}

/** Paramètres > Véhicule > Modèle : liste verticale (pas de grille de cartes, règle §2.3). */
@Composable
fun VehicleModelSection(current: VehicleKind, onSelect: (VehicleKind) -> Unit) {
    SettingsGroup {
        VehicleKind.entries.forEach { kind ->
            SettingsOptionRow(kind.label, kind.description, selected = kind == current) { onSelect(kind) }
        }
    }
}

/** Paramètres > Véhicule > Couleur : chaque ligne montre la teinte dans une pastille. */
@Composable
fun VehicleColorSection(current: VehicleColor, onSelect: (VehicleColor) -> Unit) {
    SettingsGroup {
        VehicleColor.entries.forEach { color ->
            SettingsOptionRow(
                title = color.label,
                description = null,
                selected = color == current,
                leading = { ColorSwatch(color) },
            ) { onSelect(color) }
        }
    }
}

@Composable
private fun ColorSwatch(color: VehicleColor) {
    Box(
        Modifier
            .size(28.dp)
            .background(Color(android.graphics.Color.parseColor(color.hex)), CircleShape)
            // Le liseré garde visibles les teintes sombres sur le fond sombre.
            .border(2.dp, CockpitColors.PanelRaised, CircleShape),
    )
}
