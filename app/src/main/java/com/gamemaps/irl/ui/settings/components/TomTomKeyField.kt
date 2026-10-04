package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Saisie de la clé TomTom : champ masqué, bouton COLLER (presse-papiers) et ENREGISTRER.
 * Le texte tapé reste dans ce composant ; seule une clé validée part vers [onSave].
 *
 * @param onSave renvoie false si la clé n'a pas la bonne forme (un message s'affiche alors).
 */
@Composable
fun TomTomKeyField(onSave: (String) -> Boolean) {
    var text by remember { mutableStateOf("") }
    var rejected by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; rejected = false },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Clé d'API TomTom") },
            isError = rejected,
            visualTransformation = PasswordVisualTransformation(),
            // Type "mot de passe" : le clavier ne mémorise pas la clé et ne la propose pas ailleurs.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CockpitColors.Text,
                unfocusedTextColor = CockpitColors.Text,
                focusedBorderColor = CockpitColors.Route,
                unfocusedBorderColor = CockpitColors.Border,
                focusedLabelColor = CockpitColors.Route,
                unfocusedLabelColor = CockpitColors.TextMuted,
                cursorColor = CockpitColors.Route,
            ),
        )
        if (rejected) {
            Text("Cette clé n'a pas la bonne forme (lettres et chiffres uniquement)", style = CockpitTypography.Caption, color = CockpitColors.Danger)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KeyButton("COLLER", CockpitColors.Panel, CockpitColors.Text, Modifier.weight(1f)) {
                clipboard.getText()?.text?.let { text = it.trim(); rejected = false }
            }
            KeyButton("ENREGISTRER", CockpitColors.Route, Color.Black, Modifier.weight(1f)) {
                if (onSave(text)) text = "" else rejected = true
            }
        }
    }
}

@Composable
private fun KeyButton(label: String, background: Color, textColor: Color, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .background(background, shape)
            .border(1.dp, CockpitColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = CockpitTypography.Metric, color = textColor)
    }
}
