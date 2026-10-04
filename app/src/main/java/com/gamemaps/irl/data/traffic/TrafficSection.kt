package com.gamemaps.irl.data.traffic

import com.gamemaps.irl.core.geo.LatLng

/**
 * Portion ralentie de l'itinéraire.
 *
 * @property startIndex premier point concerné dans le tracé de l'itinéraire.
 * @property endIndex dernier point concerné (inclus).
 * @property delaySeconds temps perdu sur cette portion.
 */
data class TrafficSection(
    val startIndex: Int,
    val endIndex: Int,
    val severity: TrafficSeverity,
    val delaySeconds: Double,
) {
    /** Points du tracé couverts par la portion ; vide si les indices ne correspondent pas au tracé. */
    fun pointsOf(geometry: List<LatLng>): List<LatLng> {
        val first = startIndex.coerceAtLeast(0)
        val last = endIndex.coerceAtMost(geometry.lastIndex)
        return if (last > first) geometry.subList(first, last + 1) else emptyList()
    }
}
