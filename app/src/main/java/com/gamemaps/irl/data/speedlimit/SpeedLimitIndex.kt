package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import kotlin.math.abs

/**
 * Trouve la limitation de la route sur laquelle on roule, parmi les routes chargées.
 *
 * Route retenue = la plus proche, avec une pénalité si sa direction ne correspond pas
 * à notre cap (évite de prendre la rue perpendiculaire à un carrefour).
 */
class SpeedLimitIndex(private val roads: List<RoadSegment>) {

    private val cumulative = roads.map { PolylineProjector.cumulativeDistances(it.points) }

    fun limitAt(position: LatLng, bearingDegrees: Float?): Int? = roadAt(position, bearingDegrees)?.maxSpeedKmh

    /** La route sur laquelle on roule, ou null si aucune n'est assez proche. */
    fun roadAt(position: LatLng, bearingDegrees: Float?): RoadSegment? {
        var bestScore = Double.MAX_VALUE
        var best: RoadSegment? = null
        roads.forEachIndexed { index, road ->
            val projection = PolylineProjector.project(position, road.points, cumulative[index]) ?: return@forEachIndexed
            if (projection.distanceToLineMeters > MAX_DISTANCE_METERS) return@forEachIndexed
            val segmentBearing = GeoMath.bearingDegrees(
                road.points[projection.segmentIndex],
                road.points[projection.segmentIndex + 1],
            )
            val score = projection.distanceToLineMeters + headingPenalty(bearingDegrees, segmentBearing)
            if (score < bestScore) {
                bestScore = score
                best = road
            }
        }
        return best
    }

    /** Une route se parcourt dans les deux sens : on compare les directions modulo 180°. */
    private fun headingPenalty(bearing: Float?, segmentBearing: Double): Double {
        if (bearing == null) return 0.0
        val diff = abs(((bearing - segmentBearing) % 180 + 180) % 180)
        val angle = minOf(diff, 180 - diff)
        return if (angle > 45) HEADING_PENALTY_METERS else 0.0
    }

    private companion object {
        const val MAX_DISTANCE_METERS = 25.0
        const val HEADING_PENALTY_METERS = 20.0
    }
}
