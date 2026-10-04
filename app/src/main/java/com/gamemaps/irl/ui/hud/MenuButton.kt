package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.ui.theme.CockpitColors

/** Bouton ☰ à gauche de la barre de recherche : ouvre les Paramètres. */
@Composable
fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(52.dp)
            .background(CockpitColors.Panel, shape)
            .border(1.dp, CockpitColors.Border, shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Paramètres" },
        contentAlignment = Alignment.Center,
    ) {
        Text("☰", color = CockpitColors.Text, fontSize = 22.sp)
    }
}
