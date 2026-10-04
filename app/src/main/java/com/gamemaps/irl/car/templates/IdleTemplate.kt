package com.gamemaps.irl.car.templates

import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate

/**
 * Pas de guidage : la carte suit le véhicule. Deux icônes lancent le trajet vers la maison
 * ou le travail en un appui, un bouton ouvre la recherche, un autre coupe le son.
 */
object IdleTemplate {

    fun build(
        onHome: () -> Unit,
        onWork: () -> Unit,
        onSearch: () -> Unit,
        isMuted: Boolean,
        onToggleMute: () -> Unit,
    ): Template = NavigationTemplate.Builder()
        .setActionStrip(
            CarActions.strip(
                CarActions.home(onHome),
                CarActions.work(onWork),
                CarActions.search(onSearch),
                CarActions.muteToggle(isMuted, onToggleMute),
            ),
        )
        .build()
}
