package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.routing.RoutingException
import org.json.JSONObject

/** Transforme la réponse JSON de TomTom (calculateRoute) en [Route]. */
object TomTomResponseParser {

    fun parse(json: String): Route {
        val route = JSONObject(json).optJSONArray("routes")?.optJSONObject(0)
            ?: throw RoutingException("Aucun itinéraire trouvé")

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
