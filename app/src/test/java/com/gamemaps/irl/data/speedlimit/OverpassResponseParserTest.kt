package com.gamemaps.irl.data.speedlimit

import org.junit.Assert.assertEquals
import org.junit.Test

class OverpassResponseParserTest {

    @Test
    fun `lit les routes et ignore celles sans limite exploitable`() {
        val json = """
            { "elements": [
              { "type": "way", "id": 1, "tags": { "highway": "primary", "maxspeed": "50" },
                "geometry": [ { "lat": 44.86, "lon": -0.557 }, { "lat": 44.861, "lon": -0.556 } ] },
              { "type": "way", "id": 2, "tags": { "highway": "service", "maxspeed": "none" },
                "geometry": [ { "lat": 44.86, "lon": -0.557 }, { "lat": 44.861, "lon": -0.556 } ] },
              { "type": "way", "id": 3, "tags": { "highway": "residential", "maxspeed": "FR:zone30" },
                "geometry": [ { "lat": 44.86, "lon": -0.557 } ] }
            ]}
        """.trimIndent()

        val roads = OverpassResponseParser.parse(json)
        assertEquals(1, roads.size) // way 2 : "none" ; way 3 : un seul point
        assertEquals(1L, roads[0].wayId)
        assertEquals(50, roads[0].maxSpeedKmh)
        assertEquals(2, roads[0].points.size)
    }
}
