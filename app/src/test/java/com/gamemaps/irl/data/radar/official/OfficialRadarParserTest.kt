package com.gamemaps.irl.data.radar.official

import com.gamemaps.irl.data.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfficialRadarParserTest {

    @Test
    fun `lit le format compact`() {
        val json = """
            [
              { "id": "13598", "t": "speed", "c": [2.85479, 49.95842], "s": 130, "r": "A1", "p": "HEM MONACU", "d": "PARIS VERS LILLE" },
              { "id": "12923", "t": "red_light", "c": [6.20408, 49.11907], "s": 50, "p": "METZ" },
              { "id": "999", "t": "inconnu", "c": [0.0, 0.0], "s": 50 }
            ]
        """.trimIndent()

        val radars = OfficialRadarParser.parse(json)
        assertEquals(2, radars.size) // le type inconnu est ignoré
        assertEquals("fr_13598", radars[0].id)
        assertEquals(RadarType.SPEED, radars[0].type)
        assertEquals(49.95842, radars[0].position.lat, 1e-9)
        assertEquals(2.85479, radars[0].position.lng, 1e-9)
        assertEquals(130, radars[0].maxSpeedKmh)
        assertEquals("A1", radars[0].road)
        assertEquals(RadarType.RED_LIGHT, radars[1].type)
        assertNull(radars[1].road)
    }

    /** Vérifie le vrai fichier embarqué dans l'APK. */
    @Test
    fun `la base embarquée est complète et lisible`() {
        val radars = OfficialRadarParser.parse(File("src/main/assets/radars_france.json").readText())
        assertEquals(3_350, radars.size)
        assertEquals(radars.size, radars.map { it.id }.toSet().size) // identifiants uniques
        assertTrue(RadarType.entries.all { type -> radars.any { it.type == type } })
        assertTrue(radars.all { it.maxSpeedKmh in 30..130 })
    }
}
