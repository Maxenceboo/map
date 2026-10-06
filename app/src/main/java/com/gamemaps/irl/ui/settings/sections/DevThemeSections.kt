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
import com.gamemaps.irl.data.custom.CustomThemeSpec
import com.gamemaps.irl.data.custom.ThemeColorSlot
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.ColorPickerRow
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow
import com.gamemaps.irl.ui.settings.components.SettingsTextField

/** Mode développeur > Mes thèmes : créer un thème, ou en ouvrir un pour le modifier. */
@Composable
fun DevThemesSection(themes: List<CustomThemeSpec>, usedId: String, onCreate: () -> Unit, onEdit: (String) -> Unit) {
    SettingsGroup {
        SettingsNavigationRow(Icons.Filled.Add, "Nouveau thème", null, onCreate)
    }
    if (themes.isEmpty()) {
        SettingsGroup {
            SettingsInfoRow(Icons.Filled.Info, "Aucun thème créé", "Un nouveau thème reprend les couleurs du thème actuel")
        }
    } else {
        SettingsGroup("Mes thèmes") {
            themes.forEach { theme ->
                SettingsNavigationRow(HudIcons.Palette, theme.name.ifBlank { "Thème sans nom" }, "Utilisé".takeIf { theme.id == usedId }) { onEdit(theme.id) }
            }
        }
    }
}

/**
 * Éditeur d'un thème : son nom, puis une ligne par couleur. Chaque changement est enregistré tout de suite ;
 * si le thème est celui affiché, la carte est repeinte derrière le menu.
 */
@Composable
fun DevThemeEditSection(
    theme: CustomThemeSpec,
    isUsed: Boolean,
    onChange: (CustomThemeSpec) -> Unit,
    onUse: () -> Unit,
    onDelete: () -> Unit,
) {
    // Nom gardé localement : mis à jour immédiatement à chaque frappe.
    var name by rememberSaveable(theme.id) { mutableStateOf(theme.name) }
    // Une seule couleur dépliée à la fois.
    var openSlot by rememberSaveable(theme.id) { mutableStateOf<ThemeColorSlot?>(null) }

    SettingsGroup("Nom") {
        SettingsTextField("Nom du thème", name, onValueChange = {
            name = it
            onChange(theme.copy(name = it))
        })
    }

    SettingsGroup("Couleurs") {
        ThemeColorSlot.entries.forEach { slot ->
            ColorPickerRow(
                title = slot.label,
                hex = slot.read(theme.palette),
                expanded = openSlot == slot,
                onToggle = { openSlot = slot.takeIf { openSlot != slot } },
                onPick = { hex -> onChange(theme.copy(palette = slot.write(theme.palette, hex))) },
            )
        }
    }

    SettingsGroup {
        if (isUsed) {
            SettingsInfoRow(Icons.Filled.Check, "Thème utilisé", "Modifications appliquées en direct")
        } else {
            SettingsNavigationRow(Icons.Filled.Check, "Utiliser ce thème", null, onUse)
        }
        SettingsInfoRow(Icons.Filled.Delete, "Supprimer ce thème", null, "Supprimer", onDelete)
    }
}
