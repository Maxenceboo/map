package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.routing.RoutingException
import org.json.JSONObject

/** Transforme la réponse JSON de TomTom (calculateRoute) en [Route]. */
object TomTomResponseParser {

    /** Le trajet conseillé (le premier de la réponse). */
    fun parse(json: String): Route = parseAll(json).first()

    /** Tous les trajets de la réponse : le conseillé, puis les variantes demandées avec `maxAlternatives`. */
    fun parseAll(json: String): List<Route> {
        val routes = JSONObject(json).optJSONArray("routes").objects()
        if (routes.isEmpty()) throw RoutingException("Aucun itinéraire trouvé")
        return routes.map(::parseRoute)
    }

    private fun parseRoute(route: JSONObject): Route {

        // Un seul tronçon ("leg") : l'app ne demande jamais d'étape intermédiaire.
        val geometry = route.optJSONArray("legs")?.optJSONObject(0)?.optJSONArray("points").objects()
            .map { LatLng(lat = it.getDouble("latitude"), lng = it.getDouble("longitude")) }
        if (geometry.size < 2) throw RoutingException("Itinéraire TomTom vide")

        val summary = route.getJSONObject("summary")
        val durationSeconds = summary.getDouble("travelTimeInSeconds")
        return Route(
            geometry = geometry,
            steps = TomTomStepBuilder.build(
                instructions = route.optJSONObject("guidance")?.optJSONArray("instructions").objects(),
                geometry = geometry,
                cumulativeDistances = PolylineProjector.cumulativeDistances(geometry),
                tomTomLengthMeters = summary.optDouble("lengthInMeters", 0.0),
                durationSeconds = durationSeconds,
            ),
            durationSeconds = durationSeconds,
            trafficSections = TomTomTrafficParser.parse(route.optJSONArray("sections").objects(), geometry.lastIndex),
            trafficDelaySeconds = summary.optDouble("trafficDelayInSeconds", 0.0),
            hasLiveTraffic = true,
        )
    }
}
