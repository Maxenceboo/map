package com.gamemaps.irl.car.templates

import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate

/** Pas de guidage : la carte suit le véhicule, un bouton ouvre la recherche. */
object IdleTemplate {

    fun build(onSearch: () -> Unit, isMuted: Boolean, onToggleMute: () -> Unit): Template = NavigationTemplate.Builder()
        .setActionStrip(
            CarActions.strip(
                CarActions.search(onSearch),
                CarActions.muteToggle(isMuted, onToggleMute),
            ),
        )
        .build()
}
