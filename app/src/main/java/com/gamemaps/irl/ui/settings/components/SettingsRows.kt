package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/*
 * Lignes du menu Paramètres (règle §2.3) : listes verticales, à placer dans un [SettingsGroup].
 * Chaque ligne commence par une icône dans une tuile, alignée à gauche.
 */

/** Ligne qui ouvre un sous-menu : icône, titre, valeur actuelle, chevron. */
@Composable
fun SettingsNavigationRow(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
    RowContainer(onClick) {
        IconTile(icon)
        Text(title, style = CockpitTypography.Street, color = CockpitColors.Text, modifier = Modifier.weight(1f))
        value?.let { Text(it, style = CockpitTypography.Caption, color = CockpitColors.TextMuted) }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = CockpitColors.TextMuted, modifier = Modifier.padding(start = 4.dp).size(22.dp))
    }
}

/** Ligne avec interrupteur. */
@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, description: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    RowContainer(onClick = { onCheckedChange(!checked) }) {
        IconTile(icon)
        TitleAndDescription(title, description)
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CockpitColors.OnAccent,
                checkedTrackColor = CockpitColors.Accent,
                uncheckedThumbColor = CockpitColors.TextMuted,
                uncheckedTrackColor = CockpitColors.PanelRaised,
                uncheckedBorderColor = CockpitColors.PanelRaised,
            ),
        )
    }
}

/**
 * Choix exclusif (thème, perspective, modèle…) : l'option active est en jaune, avec une coche.
 * [leading] permet de montrer un aperçu à gauche (pastille de couleur).
 */
@Composable
fun SettingsOptionRow(
    title: String,
    description: String?,
    selected: Boolean,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    RowContainer(onClick) {
        leading?.let {
            it()
            Spacer(Modifier.width(14.dp))
        }
        TitleAndDescription(title, description, titleColor = if (selected) CockpitColors.Accent else CockpitColors.Text)
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "Sélectionné", tint = CockpitColors.Accent, modifier = Modifier.padding(start = 12.dp).size(22.dp))
        }
    }
}

/** Ligne d'information avec action optionnelle à droite ("Retirer"). */
@Composable
fun SettingsInfoRow(icon: ImageVector, title: String, subtitle: String?, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    RowContainer(onClick = null) {
        IconTile(icon)
        TitleAndDescription(title, subtitle)
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = CockpitTypography.Metric,
                color = CockpitColors.Danger,
                modifier = Modifier.padding(start = 8.dp).clip(HudShapes.Pill).clickable(onClick = onAction).padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun RowContainer(onClick: (() -> Unit)?, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.TitleAndDescription(title: String, description: String?, titleColor: androidx.compose.ui.graphics.Color = CockpitColors.Text) {
    Column(Modifier.weight(1f)) {
        Text(title, style = CockpitTypography.Street, color = titleColor)
        description?.let { Text(it, style = CockpitTypography.Caption, color = CockpitColors.TextMuted) }
    }
}

/** Icône jaune dans une tuile arrondie, suivie de l'espace avant le texte. */
@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier.size(38.dp).background(CockpitColors.PanelRaised, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = CockpitColors.Accent, modifier = Modifier.size(21.dp))
    }
    Spacer(Modifier.width(14.dp))
}
