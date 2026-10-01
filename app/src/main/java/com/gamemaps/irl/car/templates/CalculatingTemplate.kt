package com.gamemaps.irl.car.templates

import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.RoutingInfo

/** Itinéraire en cours de calcul : indicateur de chargement à la place de la manœuvre. */
object CalculatingTemplate {

    fun build(onStop: () -> Unit): Template = NavigationTemplate.Builder()
        .setNavigationInfo(RoutingInfo.Builder().setLoading(true).build())
        .setActionStrip(CarActions.strip(CarActions.button("Annuler", onStop)))
        .build()
}
