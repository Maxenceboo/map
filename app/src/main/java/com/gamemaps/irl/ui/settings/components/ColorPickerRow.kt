package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.custom.ColorMath
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Une couleur réglable : la pastille, son nom et son code. Un appui déplie le choix :
 * une grille de couleurs prêtes, et un champ pour taper un code "#RRGGBB" précis.
 */
@Composable
fun ColorPickerRow(title: String, hex: String, expanded: Boolean, onToggle: () -> Unit, onPick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Swatch(hex, selected = false, onClick = null)
            Spacer(Modifier.width(14.dp))
            Text(title, style = CockpitTypography.Street, color = CockpitColors.Text, modifier = Modifier.weight(1f))
            Text(hex, style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
        }
        if (expanded) {
            Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESETS.chunked(COLUMNS).forEach { line ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        line.forEach { preset -> Swatch(preset, selected = preset == hex) { onPick(preset) } }
                    }
                }
            }
            HexField(hex, onPick)
        }
    }
}

/** Champ du code couleur : la couleur n'est appliquée que lorsque le code est complet et valide. */
@Composable
private fun HexField(hex: String, onPick: (String) -> Unit) {
    var text by remember(hex) { mutableStateOf(hex) }
    SettingsTextField(
        label = "Code couleur (#RRGGBB)",
        value = text,
        onValueChange = { typed ->
            text = typed
            ColorMath.normalizeHex(typed)?.let(onPick)
        },
    )
}

@Composable
private fun Swatch(hex: String, selected: Boolean, onClick: (() -> Unit)?) {
    Box(
        Modifier
            .size(if (onClick == null) 28.dp else 38.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(hex)))
            // Le liseré garde visibles les teintes sombres ; en jaune, il marque la couleur choisie.
            .border(2.dp, if (selected) CockpitColors.Accent else CockpitColors.PanelRaised, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    )
}

private const val COLUMNS = 7

/** Du sombre au clair, puis les teintes vives : de quoi composer une carte de nuit ou de jour. */
private val PRESETS = listOf(
    "#000000", "#0d0f14", "#10131a", "#1b2130", "#2a3242", "#3b485e", "#6b7280",
    "#9aa3b2", "#d1d5db", "#f4f6f8", "#ffffff", "#0b1d2e", "#0e7490", "#38bdf8",
    "#3c63cc", "#3b82f6", "#a855f7", "#c084fc", "#ec4899", "#ef4444", "#f97316",
    "#ffc533", "#facc15", "#84cc16", "#22c55e", "#385e1e", "#528330", "#7c5a3a",
)
