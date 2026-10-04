package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import com.gamemaps.irl.data.settings.AppSettings
import com.gamemaps.irl.map.vehicle3d.VehicleColor
import com.gamemaps.irl.map.vehicle3d.VehicleKind
import com.gamemaps.irl.ui.settings.SettingsSection
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow
import com.gamemaps.irl.ui.settings.components.SettingsOptionRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Paramètres > Véhicule : deux sous-menus (modèle, couleur) et l'interrupteur des phares. */
@Composable
fun VehicleSection(settings: AppSettings, open: (SettingsSection) -> Unit, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsNavigationRow("🚗", "Modèle", settings.vehicle.label) { open(SettingsSection.VEHICLE_MODEL) }
    SettingsNavigationRow("🎨", "Couleur", settings.vehicleColor.label) { open(SettingsSection.VEHICLE_COLOR) }
    SettingsToggleRow("💡", "Phares", "Faisceaux projetés sur la route devant le véhicule 3D", settings.headlights) { enabled ->
        onUpdate { it.copy(headlights = enabled) }
    }
}

/** Paramètres > Véhicule > Modèle : liste verticale (pas de grille de cartes, règle §2.3). */
@Composable
fun VehicleModelSection(current: VehicleKind, onSelect: (VehicleKind) -> Unit) {
    VehicleKind.entries.forEach { kind ->
        SettingsOptionRow(kind.label, kind.description, selected = kind == current) { onSelect(kind) }
    }
}

/** Paramètres > Véhicule > Couleur. */
@Composable
fun VehicleColorSection(current: VehicleColor, onSelect: (VehicleColor) -> Unit) {
    VehicleColor.entries.forEach { color ->
        SettingsOptionRow(color.label, description = null, selected = color == current) { onSelect(color) }
    }
}
