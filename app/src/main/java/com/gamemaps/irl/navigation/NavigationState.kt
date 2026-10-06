package com.gamemaps.irl.navigation

import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.navigation.trip.TripStats

/**
 * États possibles du guidage. Le téléphone et Android Auto affichent tous les deux cet état.
 *
 * Idle → Calculating → Previewing → Navigating → Arrived
 *                    ↘ Failed       (Previewing est sauté si le départ est automatique)
 */
sealed interface NavigationState {

    /** Pas de destination : simple suivi de position. */
    data object Idle : NavigationState

    /** Destination choisie, itinéraire en cours de calcul. */
    data class Calculating(val destination: Place) : NavigationState

    /**
     * Itinéraire calculé, affiché en entier : on attend que le conducteur appuie sur Démarrer.
     * [route] est le trajet choisi parmi [alternatives] (le conseillé en premier).
     */
    data class Previewing(
        val destination: Place,
        val route: Route,
        val alternatives: List<Route> = listOf(route),
    ) : NavigationState

    /** Guidage actif. [isRerouting] = recalcul en cours après une sortie d'itinéraire. */
    data class Navigating(
        val destination: Place,
        val route: Route,
        val progress: RouteProgress,
        val isRerouting: Boolean = false,
    ) : NavigationState

    /** Destination atteinte, avec le bilan du trajet ([stats]). */
    data class Arrived(val destination: Place, val stats: TripStats = TripStats()) : NavigationState

    data class Failed(val destination: Place, val message: String) : NavigationState
}
