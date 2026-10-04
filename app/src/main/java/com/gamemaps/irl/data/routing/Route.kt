package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.data.traffic.TrafficSection

/**
 * Un itinéraire calculé.
 *
 * @property geometry tracé complet, du départ à l'arrivée.
 * @property steps manœuvres dans l'ordre (la première est DEPART, la dernière ARRIVE).
 * @property durationSeconds durée estimée par le moteur de routage (bouchons compris s'il les connaît).
 * @property trafficSections portions ralenties ; vide sans trafic en temps réel.
 * @property hasLiveTraffic true si l'itinéraire tient compte du trafic du moment : il est alors recalculé de temps en temps pendant le trajet.
 * @property trafficDelaySeconds temps perdu dans les bouchons, déjà compté dans [durationSeconds].
 */
data class Route(
    val geometry: List<LatLng>,
    val steps: List<RouteStep>,
    val durationSeconds: Double,
    val trafficSections: List<TrafficSection> = emptyList(),
    val trafficDelaySeconds: Double = 0.0,
    val hasLiveTraffic: Boolean = false,
) {
    /** Distances cumulées le long du tracé (calculées une seule fois). */
    val cumulativeDistances: DoubleArray by lazy { PolylineProjector.cumulativeDistances(geometry) }

    /** Longueur totale mesurée sur le tracé. */
    val lengthMeters: Double get() = cumulativeDistances.lastOrNull() ?: 0.0
}
