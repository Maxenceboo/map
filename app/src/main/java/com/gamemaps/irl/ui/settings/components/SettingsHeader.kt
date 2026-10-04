package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * En-tête de chaque écran du menu : bouton retour rond en haut à gauche (règle §2.3), puis le titre.
 * [backLabel] (l'écran vers lequel on revient) est lu par les lecteurs d'écran.
 */
@Composable
fun SettingsHeader(title: String, backLabel: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(CockpitColors.Panel).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour : $backLabel", tint = CockpitColors.Text, modifier = Modifier.size(22.dp))
        }
        Text(title, style = CockpitTypography.Distance, color = CockpitColors.Text, modifier = Modifier.padding(top = 16.dp))
    }
}
