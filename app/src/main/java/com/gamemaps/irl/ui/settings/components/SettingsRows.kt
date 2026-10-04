package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/*
 * Lignes du menu Paramètres (règle §2.3) : listes verticales, séparateurs fins,
 * icône alignée à gauche, flèche "›" à droite pour ouvrir un sous-menu.
 */

/** Titre de groupe ("CARTE", "AUDIO"…). */
@Composable
fun SettingsGroupTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = CockpitTypography.Caption,
        color = CockpitColors.TextMuted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
    )
}

/** Ligne qui ouvre un sous-menu : icône, titre, valeur actuelle, "›". */
@Composable
fun SettingsNavigationRow(icon: String, title: String, value: String?, onClick: () -> Unit) {
    RowContainer(onClick) {
        Icon(icon)
        Text(title, style = CockpitTypography.Street, color = CockpitColors.Text, modifier = Modifier.weight(1f))
        value?.let { Text(it, style = CockpitTypography.Caption, color = CockpitColors.TextMuted) }
        Spacer(Modifier.width(8.dp))
        Text("›", style = CockpitTypography.Street, color = CockpitColors.TextMuted)
    }
}

/** Ligne avec interrupteur. */
@Composable
fun SettingsToggleRow(icon: String, title: String, description: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    RowContainer(onClick = { onCheckedChange(!checked) }) {
        Icon(icon)
        Column(Modifier.weight(1f)) {
            Text(title, style = CockpitTypography.Street, color = CockpitColors.Text)
            description?.let { Text(it, style = CockpitTypography.Caption, color = CockpitColors.TextMuted) }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = CockpitColors.Accent,
                uncheckedThumbColor = CockpitColors.TextMuted,
                uncheckedTrackColor = CockpitColors.Panel,
                uncheckedBorderColor = CockpitColors.Border,
            ),
        )
    }
}

/** Choix exclusif (thème, perspective) : coche ✓ sur l'option active. */
@Composable
fun SettingsOptionRow(title: String, description: String?, selected: Boolean, onClick: () -> Unit) {
    RowContainer(onClick) {
        Column(Modifier.weight(1f)) {
            Text(title, style = CockpitTypography.Street, color = if (selected) CockpitColors.Accent else CockpitColors.Text)
            description?.let { Text(it, style = CockpitTypography.Caption, color = CockpitColors.TextMuted) }
        }
        if (selected) Text("✓", style = CockpitTypography.Street, color = CockpitColors.Accent)
    }
}

/** Ligne d'information avec action optionnelle à droite ("Retirer"). */
@Composable
fun SettingsInfoRow(icon: String, title: String, subtitle: String?, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    RowContainer(onClick = null) {
        Icon(icon)
        Column(Modifier.weight(1f)) {
            Text(title, style = CockpitTypography.Street, color = CockpitColors.Text)
            subtitle?.let { Text(it, style = CockpitTypography.Caption, color = CockpitColors.TextMuted) }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = CockpitTypography.Caption,
                color = CockpitColors.Danger,
                modifier = Modifier.clickable(onClick = onAction).padding(8.dp),
            )
        }
    }
}

@Composable
private fun RowContainer(onClick: (() -> Unit)?, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
        HorizontalDivider(color = CockpitColors.Border, thickness = 1.dp)
    }
}

@Composable
private fun Icon(glyph: String) {
    Text(glyph, fontSize = 18.sp, modifier = Modifier.width(36.dp))
}
