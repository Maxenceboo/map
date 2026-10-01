package com.gamemaps.irl.navigation

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.routing.RouteStep

/**
 * Où en est le conducteur sur son itinéraire, recalculé à chaque position GPS.
 *
 * @property nextStep prochaine manœuvre à annoncer (null une fois l'arrivée dépassée).
 * @property distanceToNextStepMeters distance restante avant [nextStep].
 * @property distanceFromRouteMeters écart entre la position GPS et le tracé.
 */
data class RouteProgress(
    val nextStep: RouteStep?,
    val distanceToNextStepMeters: Double,
    val remainingDistanceMeters: Double,
    val remainingDurationSeconds: Double,
    val snappedPosition: LatLng,
    val distanceFromRouteMeters: Double,
)
