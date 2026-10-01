package com.gamemaps.irl.core.geo

import kotlin.math.cos

/**
 * Projette une position GPS sur un tracé.
 *
 * Chaque segment est traité dans un repère plan local (équirectangulaire), ce qui est
 * largement assez précis pour des segments routiers de quelques centaines de mètres.
 */
object PolylineProjector {

    /** Distances cumulées depuis le premier point : `result[i]` = longueur du tracé jusqu'au point i. */
    fun cumulativeDistances(line: List<LatLng>): DoubleArray {
        val result = DoubleArray(line.size)
        for (i in 1 until line.size) {
            result[i] = result[i - 1] + GeoMath.distanceMeters(line[i - 1], line[i])
        }
        return result
    }

    /** Renvoie la projection de [point] sur [line], ou null si le tracé a moins de deux points. */
    fun project(point: LatLng, line: List<LatLng>, cumulative: DoubleArray): PolylineProjection? {
        if (line.size < 2) return null
        var best: PolylineProjection? = null
        for (i in 0 until line.size - 1) {
            val snapped = closestPointOnSegment(point, line[i], line[i + 1])
            val distance = GeoMath.distanceMeters(point, snapped)
            if (best == null || distance < best.distanceToLineMeters) {
                val along = cumulative[i] + GeoMath.distanceMeters(line[i], snapped)
                best = PolylineProjection(i, snapped, distance, along)
            }
        }
        return best
    }

    private fun closestPointOnSegment(p: LatLng, a: LatLng, b: LatLng): LatLng {
        val cosLat = cos(Math.toRadians(a.lat))
        val bx = (b.lng - a.lng) * cosLat
        val by = b.lat - a.lat
        val px = (p.lng - a.lng) * cosLat
        val py = p.lat - a.lat
        val lengthSquared = bx * bx + by * by
        val t = if (lengthSquared == 0.0) 0.0 else ((px * bx + py * by) / lengthSquared).coerceIn(0.0, 1.0)
        return LatLng(a.lat + t * (b.lat - a.lat), a.lng + t * (b.lng - a.lng))
    }
}
