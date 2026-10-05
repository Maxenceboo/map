package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjection
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.navigation.snap.SnapCandidate
import kotlin.math.abs

/**
 * Trouve la route sur laquelle on roule, parmi les routes chargées : sa limitation,
 * et le point de la chaussée le plus proche (pour y aimanter le véhicule).
 *
 * Route retenue = la plus proche, avec une pénalité si sa direction ne correspond pas
 * à notre cap (évite de prendre la rue perpendiculaire à un carrefour).
 */
class SpeedLimitIndex(private val roads: List<RoadSegment>) {

    private val cumulative = roads.map { PolylineProjector.cumulativeDistances(it.points) }

    fun limitAt(position: LatLng, bearingDegrees: Float?): Int? = roadAt(position, bearingDegrees)?.maxSpeedKmh

    /** La route sur laquelle on roule, ou null si aucune n'est assez proche. */
    fun roadAt(position: LatLng, bearingDegrees: Float?): RoadSegment? = bestMatch(position, bearingDegrees)?.first

    /** Point de la route le plus proche, avec la direction de la route dans notre sens de marche. */
    fun snapAt(position: LatLng, bearingDegrees: Float?): SnapCandidate? {
        val (road, projection) = bestMatch(position, bearingDegrees) ?: return null
        val along = segmentBearing(road, projection)
        // Une route se parcourt dans les deux sens : on garde celui qui correspond à notre cap.
        val direction = if (bearingDegrees != null && angleBetween(bearingDegrees.toDouble(), along) > 90) (along + 180) % 360 else along
        return SnapCandidate(projection.snapped, direction)
    }

    private fun bestMatch(position: LatLng, bearingDegrees: Float?): Pair<RoadSegment, PolylineProjection>? {
        var bestScore = Double.MAX_VALUE
        var best: Pair<RoadSegment, PolylineProjection>? = null
        roads.forEachIndexed { index, road ->
            val projection = PolylineProjector.project(position, road.points, cumulative[index]) ?: return@forEachIndexed
            if (projection.distanceToLineMeters > MAX_DISTANCE_METERS) return@forEachIndexed
            val score = projection.distanceToLineMeters + headingPenalty(bearingDegrees, segmentBearing(road, projection))
            if (score < bestScore) {
                bestScore = score
                best = road to projection
            }
        }
        return best
    }

    private fun segmentBearing(road: RoadSegment, projection: PolylineProjection): Double =
        GeoMath.bearingDegrees(road.points[projection.segmentIndex], road.points[projection.segmentIndex + 1])

    /** Une route se parcourt dans les deux sens : on compare les directions modulo 180°. */
    private fun headingPenalty(bearing: Float?, segmentBearing: Double): Double {
        if (bearing == null) return 0.0
        val diff = abs(((bearing - segmentBearing) % 180 + 180) % 180)
        val angle = minOf(diff, 180 - diff)
        return if (angle > 45) HEADING_PENALTY_METERS else 0.0
    }

    /** Écart angulaire le plus court entre deux caps, dans [0, 180]. */
    private fun angleBetween(a: Double, b: Double): Double {
        val diff = abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }

    private companion object {
        const val MAX_DISTANCE_METERS = 25.0
        const val HEADING_PENALTY_METERS = 20.0
    }
}
