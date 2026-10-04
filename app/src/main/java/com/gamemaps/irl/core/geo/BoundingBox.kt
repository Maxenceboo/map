package com.gamemaps.irl.core.geo

import kotlin.math.cos

/** Rectangle géographique (sud, ouest, nord, est), en degrés. */
data class BoundingBox(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
) {
    fun contains(point: LatLng): Boolean =
        point.lat in south..north && point.lng in west..east

    /** Rétrécit le rectangle de [meters] de chaque côté (utile pour anticiper une sortie de zone). */
    fun shrink(meters: Double): BoundingBox {
        val dLat = metersToLatDegrees(meters)
        val dLng = metersToLngDegrees(meters, (south + north) / 2)
        return BoundingBox(south + dLat, west + dLng, north - dLat, east - dLng)
    }

    companion object {
        /** Carré de ±[radiusMeters] autour de [center]. */
        fun around(center: LatLng, radiusMeters: Double): BoundingBox {
            val dLat = metersToLatDegrees(radiusMeters)
            val dLng = metersToLngDegrees(radiusMeters, center.lat)
            return BoundingBox(center.lat - dLat, center.lng - dLng, center.lat + dLat, center.lng + dLng)
        }

        private fun metersToLatDegrees(meters: Double): Double =
            Math.toDegrees(meters / GeoMath.EARTH_RADIUS_METERS)

        private fun metersToLngDegrees(meters: Double, atLat: Double): Double =
            Math.toDegrees(meters / (GeoMath.EARTH_RADIUS_METERS * cos(Math.toRadians(atLat))))
    }
}
