package com.gamemaps.irl.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/** Garde l'écran allumé tant que cet écran est affiché (on conduit : pas de mise en veille). */
@Composable
fun KeepScreenOnEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
