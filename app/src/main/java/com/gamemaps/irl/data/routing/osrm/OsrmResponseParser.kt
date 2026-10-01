package com.gamemaps.irl.data.routing.osrm

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.core.geo.PolylineProjector
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.routing.RouteStep
import com.gamemaps.irl.data.routing.RoutingException
import org.json.JSONArray
import org.json.JSONObject

/** Transforme la réponse JSON d'OSRM (geometries=geojson, steps=true) en [Route]. */
object OsrmResponseParser {

    fun parse(json: String): Route {
        val root = JSONObject(json)
        val code = root.optString("code")
        if (code != "Ok") throw RoutingException("OSRM a répondu $code")
        val route = root.optJSONArray("routes")?.optJSONObject(0)
            ?: throw RoutingException("Aucun itinéraire trouvé")

        val geometry = parseCoordinates(route.getJSONObject("geometry").getJSONArray("coordinates"))
        val rawSteps = route.getJSONArray("legs").flatMapObjects { leg -> leg.getJSONArray("steps").objects() }

        return Route(
            geometry = geometry,
            steps = buildSteps(rawSteps, PolylineProjector.cumulativeDistances(geometry).lastOrNull() ?: 0.0),
            durationSeconds = route.getDouble("duration"),
        )
    }

    /**
     * Chaque manœuvre se situe à la somme des longueurs des étapes précédentes.
     * Les distances OSRM diffèrent légèrement de la longueur mesurée sur le tracé :
     * on les remet à l'échelle pour que les deux soient cohérentes.
     */
    private fun buildSteps(raw: List<JSONObject>, geometryLength: Double): List<RouteStep> {
        val totalStepDistance = raw.sumOf { it.getDouble("distance") }
        val scale = if (totalStepDistance > 0) geometryLength / totalStepDistance else 1.0
        var along = 0.0
        return raw.map { step ->
            val maneuver = step.getJSONObject("maneuver")
            val location = maneuver.getJSONArray("location")
            val distance = step.getDouble("distance") * scale
            RouteStep(
                maneuver = OsrmManeuverMapper.map(maneuver.optString("type"), maneuver.optStringOrNull("modifier")),
                location = LatLng(lat = location.getDouble(1), lng = location.getDouble(0)),
                roadName = step.optString("name").ifBlank { step.optString("ref") },
                roundaboutExit = if (maneuver.has("exit")) maneuver.getInt("exit") else null,
                startDistanceMeters = along,
                distanceMeters = distance,
                durationSeconds = step.getDouble("duration"),
            ).also { along += distance }
        }
    }

    private fun parseCoordinates(array: JSONArray): List<LatLng> = (0 until array.length()).map { i ->
        val point = array.getJSONArray(i)
        LatLng(lat = point.getDouble(1), lng = point.getDouble(0))
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun <T> JSONArray.flatMapObjects(transform: (JSONObject) -> List<T>): List<T> =
        objects().flatMap(transform)

    private fun JSONObject.optStringOrNull(key: String): String? = if (has(key)) getString(key) else null
}
