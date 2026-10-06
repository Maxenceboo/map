package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.gamemaps.irl.data.custom.CustomVehicleSpec
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow
import com.gamemaps.irl.ui.settings.components.SettingsSliderRow
import com.gamemaps.irl.ui.settings.components.SettingsTextField
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow
import kotlin.math.roundToInt

/** Mode développeur > Mes véhicules : créer un véhicule, ou en ouvrir un pour le modifier. */
@Composable
fun DevVehiclesSection(vehicles: List<CustomVehicleSpec>, usedId: String, onCreate: () -> Unit, onEdit: (String) -> Unit) {
    SettingsGroup {
        SettingsNavigationRow(Icons.Filled.Add, "Nouveau véhicule", null, onCreate)
    }
    if (vehicles.isEmpty()) {
        SettingsGroup {
            SettingsInfoRow(Icons.Filled.Info, "Aucun véhicule créé", "Réglez ses mesures, le modèle 3D suit")
        }
    } else {
        SettingsGroup("Mes véhicules") {
            vehicles.forEach { vehicle ->
                SettingsNavigationRow(HudIcons.Car, vehicle.name.ifBlank { "Véhicule sans nom" }, "Utilisé".takeIf { vehicle.id == usedId }) { onEdit(vehicle.id) }
            }
        }
    }
}

/**
 * Éditeur d'un véhicule : son nom, puis ses mesures. Chaque changement est enregistré tout de suite ;
 * si le véhicule est celui affiché, il est redessiné sur la carte derrière le menu.
 * La couleur de carrosserie et les phares se règlent dans Paramètres > Véhicule, comme pour les autres.
 */
@Composable
fun DevVehicleEditSection(
    vehicle: CustomVehicleSpec,
    isUsed: Boolean,
    onChange: (CustomVehicleSpec) -> Unit,
    onUse: () -> Unit,
    onDelete: () -> Unit,
) {
    // Nom gardé localement : mis à jour immédiatement à chaque frappe.
    var name by rememberSaveable(vehicle.id) { mutableStateOf(vehicle.name) }

    SettingsGroup("Nom") {
        SettingsTextField("Nom du véhicule", name, onValueChange = {
            name = it
            onChange(vehicle.copy(name = it))
        })
    }

    SettingsGroup("Carrosserie") {
        SettingsSliderRow("Longueur", meters(vehicle.length), vehicle.length, CustomVehicleSpec.LENGTH) { onChange(vehicle.copy(length = it)) }
        SettingsSliderRow("Largeur", meters(vehicle.width), vehicle.width, CustomVehicleSpec.WIDTH) { onChange(vehicle.copy(width = it)) }
        SettingsSliderRow("Hauteur de caisse", meters(vehicle.bodyHeight), vehicle.bodyHeight, CustomVehicleSpec.BODY_HEIGHT) { onChange(vehicle.copy(bodyHeight = it)) }
        SettingsSliderRow("Garde au sol", meters(vehicle.groundClearance), vehicle.groundClearance, CustomVehicleSpec.GROUND_CLEARANCE) { onChange(vehicle.copy(groundClearance = it)) }
    }

    SettingsGroup("Habitacle et roues") {
        SettingsSliderRow("Longueur de l'habitacle", "${(vehicle.cabinRatio * 100).roundToInt()} %", vehicle.cabinRatio, CustomVehicleSpec.CABIN_RATIO) { onChange(vehicle.copy(cabinRatio = it)) }
        SettingsSliderRow("Hauteur des vitres", meters(vehicle.cabinHeight), vehicle.cabinHeight, CustomVehicleSpec.CABIN_HEIGHT) { onChange(vehicle.copy(cabinHeight = it)) }
        SettingsSliderRow("Diamètre des roues", meters(vehicle.wheelDiameter), vehicle.wheelDiameter, CustomVehicleSpec.WHEEL_DIAMETER) { onChange(vehicle.copy(wheelDiameter = it)) }
        SettingsToggleRow(HudIcons.Car, "Aileron arrière", null, vehicle.spoiler) { enabled -> onChange(vehicle.copy(spoiler = enabled)) }
    }

    SettingsGroup {
        if (isUsed) {
            SettingsInfoRow(Icons.Filled.Check, "Véhicule utilisé", "Modifications appliquées en direct")
        } else {
            SettingsNavigationRow(Icons.Filled.Check, "Utiliser ce véhicule", null, onUse)
        }
        SettingsInfoRow(Icons.Filled.Delete, "Supprimer ce véhicule", null, "Supprimer", onDelete)
    }
}

/** "4,40 m" (virgule ou point selon la langue du téléphone). */
private fun meters(value: Double): String = "%.2f m".format(value)
