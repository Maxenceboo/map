package com.gamemaps.irl.navigation

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.data.routing.Route

/** Calcule la [RouteProgress] d'une position sur un itinéraire. Fonction pure, facile à tester. */
class RouteProgressCalculator {

    fun compute(route: Route, position: LatLng): RouteProgress {
        val projection = PolylineProjector.project(position, route.geometry, route.cumulativeDistances)
        val along = projection?.distanceAlongMeters ?: 0.0
        val total = route.lengthMeters

        // Une manœuvre est "passée" dès qu'on l'a dépassée de quelques mètres.
        val nextStep = route.steps.firstOrNull { it.startDistanceMeters > along + PASSED_TOLERANCE_METERS }
        val remaining = (total - along).coerceAtLeast(0.0)

        return RouteProgress(
            nextStep = nextStep,
            distanceToNextStepMeters = ((nextStep?.startDistanceMeters ?: total) - along).coerceAtLeast(0.0),
            remainingDistanceMeters = remaining,
            remainingDurationSeconds = if (total > 0) route.durationSeconds * remaining / total else 0.0,
            snappedPosition = projection?.snapped ?: position,
            distanceFromRouteMeters = projection?.distanceToLineMeters ?: 0.0,
        )
    }

    private companion object {
        const val PASSED_TOLERANCE_METERS = 5.0
    }
}
