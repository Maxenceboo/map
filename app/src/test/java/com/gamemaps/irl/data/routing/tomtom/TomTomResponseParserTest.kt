package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.RoutingException
import com.gamemaps.irl.data.traffic.TrafficSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TomTomResponseParserTest {

    /** Tracé en L de 4 points : ~500 m + ~500 m vers le nord, puis ~1000 m vers l'est. */
    private fun json(guidance: String) = """
        {
          "routes": [{
            "summary": { "lengthInMeters": 2000, "travelTimeInSeconds": 300, "trafficDelayInSeconds": 95 },
            "legs": [{ "points": [
              { "latitude": 44.84, "longitude": -0.58 },
              { "latitude": 44.8445, "longitude": -0.58 },
              { "latitude": 44.849, "longitude": -0.58 },
              { "latitude": 44.849, "longitude": -0.5673 }
            ] }],
            "sections": [
              { "startPointIndex": 0, "endPointIndex": 1, "sectionType": "TRAFFIC",
                "effectiveSpeedInKmh": 35, "delayInSeconds": 20, "magnitudeOfDelay": 1 },
              { "startPointIndex": 2, "endPointIndex": 3, "sectionType": "TRAFFIC",
                "effectiveSpeedInKmh": 9, "delayInSeconds": 75, "magnitudeOfDelay": 3 },
              { "startPointIndex": 0, "endPointIndex": 3, "sectionType": "TRAVEL_MODE", "travelMode": "car" },
              { "startPointIndex": 2, "endPointIndex": 40, "sectionType": "TRAFFIC" }
            ]
            $guidance
          }]
        }
    """.trimIndent()

    private val guidance = """
        , "guidance": { "instructions": [
          { "routeOffsetInMeters": 0, "travelTimeInSeconds": 0, "pointIndex": 0, "maneuver": "DEPART",
            "street": "Cours de l'Intendance", "point": { "latitude": 44.84, "longitude": -0.58 } },
          { "routeOffsetInMeters": 1000, "travelTimeInSeconds": 180, "pointIndex": 2, "maneuver": "ROUNDABOUT_RIGHT",
            "roundaboutExitNumber": 2, "roadNumbers": ["D1010"], "point": { "latitude": 44.849, "longitude": -0.58 } },
          { "routeOffsetInMeters": 2000, "travelTimeInSeconds": 300, "pointIndex": 3, "maneuver": "ARRIVE",
            "point": { "latitude": 44.849, "longitude": -0.5673 } }
        ] }
    """.trimIndent()

    @Test
    fun `lit le tracé, la durée et le retard`() {
        val route = TomTomResponseParser.parse(json(guidance))
        assertEquals(4, route.geometry.size)
        assertEquals(44.849, route.geometry[2].lat, 1e-9)
        assertEquals(-0.5673, route.geometry[3].lng, 1e-9)
        assertEquals(300.0, route.durationSeconds, 1e-9)
        assertEquals(95.0, route.trafficDelaySeconds, 1e-9)
    }

    @Test
    fun `garde les portions de trafic valides et les classe`() {
        val sections = TomTomResponseParser.parse(json(guidance)).trafficSections
        // La section TRAVEL_MODE et celle qui sort du tracé sont ignorées.
        assertEquals(2, sections.size)
        assertEquals(TrafficSeverity.SLOW, sections[0].severity)
        assertEquals(TrafficSeverity.JAM, sections[1].severity)
        assertEquals(2 to 3, sections[1].startIndex to sections[1].endIndex)
    }

    @Test
    fun `place les étapes le long du tracé`() {
        val route = TomTomResponseParser.parse(json(guidance))
        val steps = route.steps
        assertEquals(listOf(ManeuverType.DEPART, ManeuverType.ROUNDABOUT, ManeuverType.ARRIVE), steps.map { it.maneuver })
        assertEquals("Cours de l'Intendance", steps[0].roadName)
        assertEquals("D1010", steps[1].roadName)
        assertEquals(2, steps[1].roundaboutExit)
        assertEquals(route.cumulativeDistances[2], steps[1].startDistanceMeters, 1e-6)
        assertEquals(route.lengthMeters, steps[2].startDistanceMeters, 1e-6)
        assertEquals(180.0, steps[0].durationSeconds, 1e-9)
        assertEquals(route.lengthMeters, steps.sumOf { it.distanceMeters }, 1e-6)
    }

    @Test
    fun `sans instructions - départ et arrivée seulement`() {
        val route = TomTomResponseParser.parse(json(guidance = ""))
        assertEquals(listOf(ManeuverType.DEPART, ManeuverType.ARRIVE), route.steps.map { it.maneuver })
        assertEquals(route.lengthMeters, route.steps[0].distanceMeters, 1e-6)
    }

    @Test
    fun `réponse sans itinéraire`() {
        val error = runCatching { TomTomResponseParser.parse("""{ "routes": [] }""") }.exceptionOrNull()
        assertTrue(error is RoutingException)
    }
}
