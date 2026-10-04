package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Champ de texte des Paramètres (nom d'un thème, d'un véhicule…), à placer dans un [SettingsGroup]. */
@Composable
fun SettingsTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        singleLine = true,
        shape = HudShapes.Button,
        label = { Text(label) },
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
}

/** Réglage par curseur : le titre, la valeur lisible à droite ("4,40 m"), puis le curseur. */
@Composable
fun SettingsSliderRow(
    title: String,
    valueLabel: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    onValueChange: (Double) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(title, style = CockpitTypography.Street, color = CockpitColors.Text, modifier = Modifier.weight(1f))
            Text(valueLabel, style = CockpitTypography.Metric, color = CockpitColors.Accent)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = CockpitColors.Accent,
                activeTrackColor = CockpitColors.Accent,
                inactiveTrackColor = CockpitColors.PanelRaised,
            ),
        )
    }
}
