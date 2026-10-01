package com.gamemaps.irl.car.templates

import android.graphics.Color
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.RoutingInfo
import com.gamemaps.irl.car.mapping.CarDistanceMapper
import com.gamemaps.irl.car.mapping.CarStepMapper
import com.gamemaps.irl.car.mapping.CarTravelEstimateMapper
import com.gamemaps.irl.navigation.NavigationState

/**
 * Guidage actif : carte avec, par-dessus, la manœuvre (gérée par Android Auto),
 * l'estimation d'arrivée et le bouton "Arrêter".
 */
object NavigatingTemplate {

    /** Fond violet de la carte de manœuvre, couleur de l'itinéraire (§5.2). */
    private val ROUTE_PURPLE = Color.parseColor("#7e22ce")

    fun build(state: NavigationState.Navigating, onStop: () -> Unit): Template {
        val progress = state.progress
        val nextStep = progress.nextStep
        val routingInfo = if (nextStep == null || state.isRerouting) {
            RoutingInfo.Builder().setLoading(true).build()
        } else {
            RoutingInfo.Builder()
                .setCurrentStep(CarStepMapper.map(nextStep), CarDistanceMapper.map(progress.distanceToNextStepMeters))
                .build()
        }
        return NavigationTemplate.Builder()
            .setNavigationInfo(routingInfo)
            .setDestinationTravelEstimate(CarTravelEstimateMapper.map(progress))
            .setBackgroundColor(CarColor.createCustom(ROUTE_PURPLE, ROUTE_PURPLE))
            .setActionStrip(CarActions.strip(CarActions.button("Arrêter", onStop)))
            .build()
    }
}
