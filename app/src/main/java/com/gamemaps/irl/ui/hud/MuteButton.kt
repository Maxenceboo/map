package com.gamemaps.irl.ui.hud

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gamemaps.irl.ui.components.HudIconButton
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.theme.CockpitColors

/** Bouton rond du son, hors guidage : haut-parleur, barré en rouge quand le son est coupé. */
@Composable
fun MuteButton(isMuted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    HudIconButton(
        icon = if (isMuted) HudIcons.VolumeOff else HudIcons.VolumeOn,
        description = if (isMuted) "Réactiver le son" else "Couper le son",
        onClick = onToggle,
        modifier = modifier,
        tint = if (isMuted) CockpitColors.Danger else CockpitColors.Text,
    )
}
