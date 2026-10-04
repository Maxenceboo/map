package com.gamemaps.irl.ui.settings.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsNavigationRow
import com.gamemaps.irl.ui.settings.components.SettingsOptionRow
import com.gamemaps.irl.ui.theme.CockpitColors

/**
 * Choix d'une adresse pour Maison, Travail ou un nouveau favori : le lieu actuel (avec "Retirer"),
 * puis une recherche d'adresse ou la position où se trouve le téléphone.
 *
 * @param current lieu déjà enregistré, ou null (toujours null pour un nouveau favori).
 * @param onUseCurrentPosition null tant que le GPS n'a pas de position.
 */
@Composable
fun PlacePickerSection(
    current: Place?,
    results: List<Place>,
    onSearch: (String) -> Unit,
    onPick: (Place) -> Unit,
    onUseCurrentPosition: (() -> Unit)?,
    onClear: () -> Unit = {},
) {
    // Texte gardé localement : mis à jour immédiatement à chaque frappe.
    var query by rememberSaveable { mutableStateOf("") }

    if (current != null) {
        SettingsGroup("Adresse actuelle") {
            SettingsInfoRow(Icons.Filled.Place, current.name, current.subtitle.ifBlank { null }, "Retirer", onClear)
        }
    }

    SettingsGroup(if (current == null) "Choisir une adresse" else "Changer d'adresse") {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onSearch(it)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            singleLine = true,
            shape = HudShapes.Button,
            label = { Text("Adresse ou lieu") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CockpitColors.Text,
                unfocusedTextColor = CockpitColors.Text,
                focusedBorderColor = CockpitColors.Accent,
                unfocusedBorderColor = CockpitColors.Border,
                focusedLabelColor = CockpitColors.Accent,
                unfocusedLabelColor = CockpitColors.TextMuted,
                cursorColor = CockpitColors.Accent,
            ),
        )
        if (onUseCurrentPosition != null) {
            SettingsNavigationRow(HudIcons.Recenter, "Ma position actuelle", null, onUseCurrentPosition)
        }
    }

    if (results.isNotEmpty()) {
        SettingsGroup("Résultats") {
            results.forEach { place ->
                SettingsOptionRow(place.name, place.subtitle.ifBlank { null }, selected = false) { onPick(place) }
            }
        }
    }
}
