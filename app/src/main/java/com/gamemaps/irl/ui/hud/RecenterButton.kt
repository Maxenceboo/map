package com.gamemaps.irl.ui.hud

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gamemaps.irl.ui.components.HudIconButton
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.theme.CockpitColors

/** Apparaît quand la carte a été déplacée au doigt : ramène la caméra sur le véhicule. */
@Composable
fun RecenterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    HudIconButton(
        icon = HudIcons.Recenter,
        description = "Recentrer la carte",
        onClick = onClick,
        modifier = modifier,
        tint = CockpitColors.OnAccent,
        background = CockpitColors.Accent,
    )
}
