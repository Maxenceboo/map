package com.gamemaps.irl.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedingDetectorTest {

    @Test
    fun `tolérance de 3 km par heure`() {
        assertFalse(SpeedingDetector.isSpeeding(speedKmh = 53, limitKmh = 50))
        assertTrue(SpeedingDetector.isSpeeding(speedKmh = 54, limitKmh = 50))
    }

    @Test
    fun `pas d'alerte sans limite connue`() {
        assertFalse(SpeedingDetector.isSpeeding(speedKmh = 200, limitKmh = null))
    }
}
