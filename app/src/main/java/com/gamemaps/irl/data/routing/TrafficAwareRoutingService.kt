package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng
import kotlinx.coroutines.CancellationException

/**
 * Choisit le moteur de routage : celui avec trafic (TomTom) quand il est disponible et activé,
 * sinon le moteur de secours (OSRM). Si le premier échoue (réseau, clé refusée, quota dépassé),
 * on retombe sur le second : le conducteur a toujours un itinéraire.
 *
 * @param withTraffic interrogé à chaque calcul ; null quand aucune clé TomTom n'est enregistrée.
 * @param trafficEnabled réglage "Trafic en temps réel" des Paramètres.
 */
class TrafficAwareRoutingService(
    private val withTraffic: () -> RoutingService?,
    private val fallback: RoutingService,
    private val trafficEnabled: () -> Boolean,
) : RoutingService {

    override suspend fun route(from: LatLng, to: LatLng): Route = ask { it.route(from, to) }

    override suspend fun alternatives(from: LatLng, to: LatLng): List<Route> = ask { it.alternatives(from, to) }

    /** Pose la question au moteur avec trafic, puis au moteur de secours s'il n'est pas disponible ou échoue. */
    private suspend fun <T> ask(question: suspend (RoutingService) -> T): T {
        val trafficRouting = withTraffic().takeIf { trafficEnabled() } ?: return question(fallback)
        return try {
            question(trafficRouting)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            question(fallback)
        }
    }
}
