package com.gamemaps.irl.navigation.snap

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.routing.Route

/**
 * Aimante le véhicule à la route : le GPS dérive de quelques mètres, ce qui le ferait rouler
 * à côté de la chaussée. La position affichée est ramenée sur la route, et le véhicule est
 * orienté dans son axe.
 *
 * - Pendant un guidage : on colle à l'itinéraire tant qu'on en est à moins de [maxRouteMeters].
 * - Sinon : on colle à la route la plus proche fournie par `nearestRoad` (routes déjà chargées
 *   pour les limitations de vitesse).
 * - L'aimantation démarre en roulant et tient à l'arrêt (feu rouge) ; garé loin d'une route,
 *   le véhicule reste où le GPS le place.
 *
 * Ne sert qu'à l'affichage : le guidage continue de raisonner sur la position GPS réelle.
 */
class RoadSnapper(
    private val maxRouteMeters: Double = 35.0,
    private val minSpeedMetersPerSecond: Float = 1.5f,
) {
    /** true une fois le véhicule posé sur une route. */
    private var engaged = false

    fun snap(fix: GpsFix, route: Route?, nearestRoad: (LatLng, Float?) -> SnapCandidate?): GpsFix {
        val candidate = route?.let { onRoute(fix, it) } ?: nearestRoad(fix.position, fix.bearingDegrees)
        val moving = (fix.speedMetersPerSecond ?: 0f) >= minSpeedMetersPerSecond
        if (moving) engaged = candidate != null
        if (!engaged || candidate == null) return fix
        return fix.copy(
            position = candidate.position,
            // À l'arrêt, on garde le dernier cap : la route n'a pas de sens de marche à imposer.
            bearingDegrees = if (moving) candidate.bearingDegrees.toFloat() else fix.bearingDegrees,
        )
    }

    private fun onRoute(fix: GpsFix, route: Route): SnapCandidate? {
        val geometry = route.geometry
        val projection = PolylineProjector.project(fix.position, geometry, route.cumulativeDistances) ?: return null
        if (projection.distanceToLineMeters > maxRouteMeters) return null
        val i = projection.segmentIndex
        return SnapCandidate(projection.snapped, GeoMath.bearingDegrees(geometry[i], geometry[i + 1]))
    }
}
