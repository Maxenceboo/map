package com.gamemaps.irl.data.speedlimit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSpeedLimitsTest {

    @Test
    fun `valeurs par type de route`() {
        assertEquals(130, DefaultSpeedLimits.estimate(mapOf("highway" to "motorway")))
        assertEquals(110, DefaultSpeedLimits.estimate(mapOf("highway" to "trunk")))
        assertEquals(50, DefaultSpeedLimits.estimate(mapOf("highway" to "residential")))
        assertEquals(20, DefaultSpeedLimits.estimate(mapOf("highway" to "living_street")))
        assertEquals(80, DefaultSpeedLimits.estimate(mapOf("highway" to "secondary")))
    }

    @Test
    fun `une départementale en agglomération passe à 50`() {
        assertEquals(50, DefaultSpeedLimits.estimate(mapOf("highway" to "secondary", "lit" to "yes")))
        assertEquals(50, DefaultSpeedLimits.estimate(mapOf("highway" to "tertiary", "source:maxspeed" to "FR:urban")))
    }

    @Test
    fun `les chemins et pistes n'ont pas de limitation`() {
        assertNull(DefaultSpeedLimits.estimate(mapOf("highway" to "footway")))
        assertNull(DefaultSpeedLimits.estimate(emptyMap()))
    }

    @Test
    fun `une route sans panneau renseigné est marquée comme estimée`() {
        val json = """
            { "elements": [
              { "type": "way", "id": 1, "tags": { "highway": "residential" },
                "geometry": [ { "lat": 44.84, "lon": -0.58 }, { "lat": 44.841, "lon": -0.58 } ] },
              { "type": "way", "id": 2, "tags": { "highway": "residential", "maxspeed": "30" },
                "geometry": [ { "lat": 44.84, "lon": -0.57 }, { "lat": 44.841, "lon": -0.57 } ] },
              { "type": "way", "id": 3, "tags": { "highway": "cycleway" },
                "geometry": [ { "lat": 44.84, "lon": -0.56 }, { "lat": 44.841, "lon": -0.56 } ] }
            ] }
        """.trimIndent()
        val roads = OverpassResponseParser.parse(json)
        assertEquals(listOf(1L, 2L), roads.map { it.wayId })
        assertEquals(50, roads[0].maxSpeedKmh)
        assertTrue(roads[0].estimated)
        assertEquals(30, roads[1].maxSpeedKmh)
        assertTrue(!roads[1].estimated)
    }
}
