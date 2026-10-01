package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gamemaps.irl.ui.components.CockpitPanel
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Bandeau texte simple (calcul en cours, erreur, arrivée). Un appui le ferme si [onDismiss] est fourni. */
@Composable
fun StatusBanner(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CockpitColors.Text,
    onDismiss: (() -> Unit)? = null,
) {
    val clickModifier = if (onDismiss != null) Modifier.clickable(onClick = onDismiss) else Modifier
    CockpitPanel(modifier.fillMaxWidth().then(clickModifier)) {
        Text(text = text, style = CockpitTypography.Street, color = color)
    }
}
