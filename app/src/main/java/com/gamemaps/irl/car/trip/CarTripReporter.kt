package com.gamemaps.irl.car.trip

import androidx.car.app.CarContext
import androidx.car.app.navigation.NavigationManager
import androidx.car.app.navigation.NavigationManagerCallback
import androidx.car.app.navigation.model.Destination
import androidx.car.app.navigation.model.Trip
import com.gamemaps.irl.car.mapping.CarStepMapper
import com.gamemaps.irl.car.mapping.CarTravelEstimateMapper
import com.gamemaps.irl.navigation.NavigationState

/**
 * Informe Android Auto qu'un guidage est en cours (obligatoire pour une app de navigation).
 * Le système peut alors afficher la prochaine manœuvre ailleurs (tableau de bord, notifications)
 * et nous demander d'arrêter si une autre app démarre un guidage.
 */
class CarTripReporter(carContext: CarContext, onStopRequested: () -> Unit) {

    private val manager = carContext.getCarService(NavigationManager::class.java)
    private var started = false

    init {
        manager.setNavigationManagerCallback(object : NavigationManagerCallback {
            override fun onStopNavigation() = onStopRequested()
        })
    }

    fun onStateChanged(state: NavigationState) {
        if (state !is NavigationState.Navigating) {
            // Calculating : on garde l'état courant ; tout le reste termine le guidage.
            if (state !is NavigationState.Calculating) end()
            return
        }
        if (!started) {
            manager.navigationStarted()
            started = true
        }
        manager.updateTrip(buildTrip(state))
    }

    fun release() {
        end()
        manager.clearNavigationManagerCallback()
    }

    private fun end() {
        if (!started) return
        manager.navigationEnded()
        started = false
    }

    private fun buildTrip(state: NavigationState.Navigating): Trip {
        val estimate = CarTravelEstimateMapper.map(state.progress)
        val destination = Destination.Builder()
            .setName(state.destination.name)
            .setAddress(state.destination.subtitle.ifBlank { state.destination.name })
            .build()
        val builder = Trip.Builder().addDestination(destination, estimate).setLoading(state.isRerouting)
        state.progress.nextStep?.let { builder.addStep(CarStepMapper.map(it), estimate) }
        return builder.build()
    }
}
