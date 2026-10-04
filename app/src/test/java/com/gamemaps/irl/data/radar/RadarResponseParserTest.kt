package com.gamemaps.irl.data.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RadarResponseParserTest {

    @Test
    fun `lit position et vitesse contrôlée`() {
        val json = """
            { "elements": [
              { "type": "node", "id": 10, "lat": 44.86, "lon": -0.55, "tags": { "highway": "speed_camera", "maxspeed": "90" } },
              { "type": "node", "id": 11, "lat": 44.87, "lon": -0.56, "tags": { "highway": "speed_camera" } },
              { "type": "node", "id": 12 }
            ]}
        """.trimIndent()

        val radars = RadarResponseParser.parse(json)
        assertEquals(2, radars.size) // le nœud 12 n'a pas de coordonnées
        assertEquals(90, radars[0].maxSpeedKmh)
        assertEquals(44.86, radars[0].position.lat, 1e-9)
        assertNull(radars[1].maxSpeedKmh)
    }
}
