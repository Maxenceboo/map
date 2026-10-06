package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng

/** Calcul d'itinéraire voiture. Lève une exception si aucun itinéraire n'est trouvé. */
fun interface RoutingService {

    suspend fun route(from: LatLng, to: LatLng): Route

    /**
     * Plusieurs trajets possibles, le trajet conseillé en premier, pour laisser le choix avant le départ.
     * Par défaut : le seul trajet de [route].
     */
    suspend fun alternatives(from: LatLng, to: LatLng): List<Route> = listOf(route(from, to))
}
