package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng

/** Calcul d'itinéraire voiture. Lève une exception si aucun itinéraire n'est trouvé. */
fun interface RoutingService {
    suspend fun route(from: LatLng, to: LatLng): Route
}
