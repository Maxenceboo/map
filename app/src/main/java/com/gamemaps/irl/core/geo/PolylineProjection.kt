package com.gamemaps.irl.core.geo

/**
 * Résultat de la projection d'un point sur une polyligne (le tracé d'un itinéraire).
 *
 * @property segmentIndex index du segment [i, i+1] le plus proche.
 * @property snapped point de la polyligne le plus proche de la position.
 * @property distanceToLineMeters écart entre la position et le tracé.
 * @property distanceAlongMeters distance parcourue depuis le début du tracé jusqu'à [snapped].
 */
data class PolylineProjection(
    val segmentIndex: Int,
    val snapped: LatLng,
    val distanceToLineMeters: Double,
    val distanceAlongMeters: Double,
)
