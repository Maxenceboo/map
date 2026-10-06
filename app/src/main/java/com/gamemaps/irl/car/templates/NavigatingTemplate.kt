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
 * l'estimation d'arrivée et les boutons : changer de destination, son, croix pour arrêter (avec confirmation).
 */
object NavigatingTemplate {

    /** Fond de la carte de manœuvre : le bleu nuit des surfaces du HUD (la flèche est jaune). */
    private val PANEL = Color.parseColor("#1F2630")

    fun build(
        state: NavigationState.Navigating,
        isMuted: Boolean,
        confirmStop: Boolean,
        onSearch: () -> Unit,
        onToggleMute: () -> Unit,
        onAskStop: () -> Unit,
        onCancelStop: () -> Unit,
        onStop: () -> Unit,
    ): Template {
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
            .setBackgroundColor(CarColor.createCustom(PANEL, PANEL))
            .setActionStrip(
                // La croix ne coupe pas le trajet d'un coup : elle propose "Arrêter" ou "Continuer".
                if (confirmStop) {
                    CarActions.strip(CarActions.button("Continuer", onCancelStop), CarActions.button("Arrêter", onStop))
                } else {
                    CarActions.strip(CarActions.search(onSearch), CarActions.muteToggle(isMuted, onToggleMute), CarActions.cancelTrip(onAskStop))
                },
            )
            .build()
    }
}
