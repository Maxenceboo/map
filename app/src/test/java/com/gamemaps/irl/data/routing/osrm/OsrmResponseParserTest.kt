package com.gamemaps.irl.data.routing.osrm

import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.RoutingException
import org.junit.Assert.assertEquals
import org.junit.Test

class OsrmResponseParserTest {

    private val json = """
        {
          "code": "Ok",
          "routes": [{
            "duration": 120.0,
            "distance": 2000.0,
            "geometry": { "type": "LineString", "coordinates": [[-0.58, 44.84], [-0.58, 44.849], [-0.5673, 44.849]] },
            "legs": [{
              "steps": [
                { "distance": 1000.0, "duration": 60.0, "name": "Cours de l'Intendance",
                  "maneuver": { "type": "depart", "location": [-0.58, 44.84] } },
                { "distance": 1000.0, "duration": 60.0, "name": "",  "ref": "D1010",
                  "maneuver": { "type": "roundabout", "modifier": "right", "exit": 2, "location": [-0.58, 44.849] } },
                { "distance": 0.0, "duration": 0.0, "name": "",
                  "maneuver": { "type": "arrive", "location": [-0.5673, 44.849] } }
              ]
            }]
          }]
        }
    """.trimIndent()

    @Test
    fun `lit le tracé et les étapes`() {
        val route = OsrmResponseParser.parse(json)
        assertEquals(3, route.geometry.size)
        assertEquals(44.849, route.geometry[1].lat, 1e-9)
        assertEquals(-0.58, route.geometry[1].lng, 1e-9)
        assertEquals(120.0, route.durationSeconds, 1e-9)
        assertEquals(listOf(ManeuverType.DEPART, ManeuverType.ROUNDABOUT, ManeuverType.ARRIVE), route.steps.map { it.maneuver })
    }

    @Test
    fun `numéro de sortie et ref utilisée quand le nom est vide`() {
        val roundabout = OsrmResponseParser.parse(json).steps[1]
        assertEquals(2, roundabout.roundaboutExit)
        assertEquals("D1010", roundabout.roadName)
    }

    @Test
    fun `les positions des manœuvres sont recalées sur la longueur du tracé`() {
        val route = OsrmResponseParser.parse(json)
        assertEquals(0.0, route.steps[0].startDistanceMeters, 1e-9)
        assertEquals(route.cumulativeDistances[1], route.steps[1].startDistanceMeters, 15.0)
        assertEquals(route.lengthMeters, route.steps[2].startDistanceMeters, 1e-6)
    }

    @Test(expected = RoutingException::class)
    fun `code d'erreur OSRM`() {
        OsrmResponseParser.parse("""{ "code": "NoRoute", "routes": [] }""")
    }

    @Test
    fun `lit tous les trajets proposés`() {
        val twoRoutes = json.replace(""""routes": [{""", """"routes": [{ "duration": 300.0, "distance": 2000.0,
            "geometry": { "type": "LineString", "coordinates": [[-0.58, 44.84], [-0.5673, 44.849]] },
            "legs": [{ "steps": [
              { "distance": 2000.0, "duration": 300.0, "name": "", "maneuver": { "type": "depart", "location": [-0.58, 44.84] } },
              { "distance": 0.0, "duration": 0.0, "name": "", "maneuver": { "type": "arrive", "location": [-0.5673, 44.849] } }
            ] }] }, {""")
        val routes = OsrmResponseParser.parseAll(twoRoutes)
        assertEquals(2, routes.size)
        assertEquals(300.0, routes[0].durationSeconds, 1e-9)
        assertEquals(120.0, routes[1].durationSeconds, 1e-9)
    }
}
