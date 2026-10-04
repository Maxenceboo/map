package com.gamemaps.irl.car.templates

import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import com.gamemaps.irl.car.mapping.CarTravelEstimateMapper
import com.gamemaps.irl.navigation.NavigationState

/**
 * Itinéraire calculé depuis le téléphone, en attente de départ :
 * estimation d'arrivée + boutons Démarrer / Annuler.
 * (Choisi depuis la voiture, le guidage démarre directement : pas d'écran en plus au volant.)
 */
object PreviewTemplate {

    fun build(state: NavigationState.Previewing, onStart: () -> Unit, onCancel: () -> Unit): Template =
        NavigationTemplate.Builder()
            .setDestinationTravelEstimate(CarTravelEstimateMapper.forRoute(state.route))
            .setActionStrip(
                CarActions.strip(
                    CarActions.button("Démarrer", onStart),
                    CarActions.button("Annuler", onCancel),
                ),
            )
            .build()
}
