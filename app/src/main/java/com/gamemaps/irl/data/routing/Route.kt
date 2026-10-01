package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector

/**
 * Un itinéraire calculé.
 *
 * @property geometry tracé complet, du départ à l'arrivée.
 * @property steps manœuvres dans l'ordre (la première est DEPART, la dernière ARRIVE).
 * @property durationSeconds durée estimée par le moteur de routage.
 */
data class Route(
    val geometry: List<LatLng>,
    val steps: List<RouteStep>,
    val durationSeconds: Double,
) {
    /** Distances cumulées le long du tracé (calculées une seule fois). */
    val cumulativeDistances: DoubleArray by lazy { PolylineProjector.cumulativeDistances(geometry) }

    /** Longueur totale mesurée sur le tracé. */
    val lengthMeters: Double get() = cumulativeDistances.lastOrNull() ?: 0.0
}
