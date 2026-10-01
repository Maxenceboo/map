package com.gamemaps.irl.navigation

import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.search.Place

/** États possibles du guidage. Le téléphone et Android Auto affichent tous les deux cet état. */
sealed interface NavigationState {

    /** Pas de destination : simple suivi de position. */
    data object Idle : NavigationState

    /** Destination choisie, itinéraire en cours de calcul. */
    data class Calculating(val destination: Place) : NavigationState

    /** Guidage actif. [isRerouting] = recalcul en cours après une sortie d'itinéraire. */
    data class Navigating(
        val destination: Place,
        val route: Route,
        val progress: RouteProgress,
        val isRerouting: Boolean = false,
    ) : NavigationState

    data class Arrived(val destination: Place) : NavigationState

    data class Failed(val destination: Place, val message: String) : NavigationState
}
