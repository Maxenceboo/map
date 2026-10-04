package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Message en haut de l'écran : une attente ([loading], indicateur qui tourne) ou une erreur.
 * Avec [onDismiss], une croix à droite ferme le message (et annule le trajet en cours de calcul).
 */
@Composable
fun StatusBanner(text: String, modifier: Modifier = Modifier, loading: Boolean = false, onDismiss: (() -> Unit)? = null) {
    CockpitPanel(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = CockpitColors.Accent, strokeWidth = 3.dp)
            } else {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = CockpitColors.Danger, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(text, style = CockpitTypography.Street, color = CockpitColors.Text, modifier = Modifier.weight(1f))
            if (onDismiss != null) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = if (loading) "Annuler le trajet" else "Fermer",
                    tint = CockpitColors.Text,
                    modifier = Modifier.padding(start = 8.dp).clip(CircleShape).background(CockpitColors.PanelRaised).clickable(onClick = onDismiss).padding(8.dp).size(22.dp),
                )
            }
        }
    }
}
