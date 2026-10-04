package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng
import kotlinx.coroutines.CancellationException

/**
 * Choisit le moteur de routage : celui avec trafic (TomTom) quand il est disponible et activé,
 * sinon le moteur de secours (OSRM). Si le premier échoue (réseau, clé refusée, quota dépassé),
 * on retombe sur le second : le conducteur a toujours un itinéraire.
 *
 * @param withTraffic null quand aucune clé TomTom n'est installée.
 * @param trafficEnabled réglage "Trafic en temps réel" des Paramètres.
 */
class TrafficAwareRoutingService(
    private val withTraffic: RoutingService?,
    private val fallback: RoutingService,
    private val trafficEnabled: () -> Boolean,
) : RoutingService {

    override suspend fun route(from: LatLng, to: LatLng): Route {
        if (withTraffic == null || !trafficEnabled()) return fallback.route(from, to)
        return try {
            withTraffic.route(from, to)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fallback.route(from, to)
        }
    }
}
