package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.RouteStep
import org.json.JSONObject

/** Construit les étapes de guidage à partir des instructions TomTom (`guidance.instructions`). */
object TomTomStepBuilder {

    /**
     * @param cumulativeDistances distance depuis le départ de chaque point du tracé.
     * @param tomTomLengthMeters longueur annoncée par TomTom, légèrement différente de celle mesurée sur le tracé.
     */
    fun build(
        instructions: List<JSONObject>,
        geometry: List<LatLng>,
        cumulativeDistances: DoubleArray,
        tomTomLengthMeters: Double,
        durationSeconds: Double,
    ): List<RouteStep> {
        val totalMeters = cumulativeDistances.last()
        if (instructions.isEmpty()) return departAndArrive(geometry, totalMeters, durationSeconds)

        val scale = if (tomTomLengthMeters > 0) totalMeters / tomTomLengthMeters else 1.0
        // Position de chaque manœuvre le long du tracé, toujours croissante.
        var previous = 0.0
        val positions = instructions.map { instruction ->
            positionAlong(instruction, cumulativeDistances, scale).coerceIn(previous, totalMeters).also { previous = it }
        }

        return instructions.mapIndexed { i, instruction ->
            val next = instructions.getOrNull(i + 1)
            val point = instruction.getJSONObject("point")
            RouteStep(
                maneuver = TomTomManeuverMapper.map(instruction.optString("maneuver")),
                location = LatLng(lat = point.getDouble("latitude"), lng = point.getDouble("longitude")),
                roadName = roadName(instruction),
                roundaboutExit = instruction.optInt("roundaboutExitNumber", 0).takeIf { it > 0 },
                startDistanceMeters = positions[i],
                distanceMeters = if (next == null) 0.0 else positions[i + 1] - positions[i],
                durationSeconds = if (next == null) 0.0 else {
                    (next.optDouble("travelTimeInSeconds", 0.0) - instruction.optDouble("travelTimeInSeconds", 0.0)).coerceAtLeast(0.0)
                },
            )
        }
    }

    /** Le numéro du point dans le tracé est exact ; à défaut, la distance annoncée remise à l'échelle. */
    private fun positionAlong(instruction: JSONObject, cumulativeDistances: DoubleArray, scale: Double): Double {
        val pointIndex = instruction.optInt("pointIndex", -1)
        return if (pointIndex in cumulativeDistances.indices) {
            cumulativeDistances[pointIndex]
        } else {
            instruction.optDouble("routeOffsetInMeters", 0.0) * scale
        }
    }

    /** Nom de la rue, sinon numéro de la route ("A10"). */
    private fun roadName(instruction: JSONObject): String =
        instruction.optString("street").ifBlank { instruction.optJSONArray("roadNumbers")?.optString(0).orEmpty() }

    /** TomTom n'a pas fourni d'instructions : on garde au moins le départ et l'arrivée. */
    private fun departAndArrive(geometry: List<LatLng>, totalMeters: Double, durationSeconds: Double) = listOf(
        RouteStep(ManeuverType.DEPART, geometry.first(), "", null, 0.0, totalMeters, durationSeconds),
        RouteStep(ManeuverType.ARRIVE, geometry.last(), "", null, totalMeters, 0.0, 0.0),
    )
}
