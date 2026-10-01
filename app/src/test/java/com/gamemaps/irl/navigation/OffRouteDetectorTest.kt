package com.gamemaps.irl.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffRouteDetectorTest {

    @Test
    fun `un écart bref ne déclenche pas de recalcul`() {
        val detector = OffRouteDetector(thresholdMeters = 35.0, graceMillis = 3_000)
        assertFalse(detector.update(50.0, timeMillis = 0))
        assertFalse(detector.update(50.0, timeMillis = 2_000))
        assertFalse(detector.update(10.0, timeMillis = 2_500)) // revenu sur le tracé
        assertFalse(detector.update(50.0, timeMillis = 4_000))
    }

    @Test
    fun `un écart de plus de 3 s déclenche le recalcul`() {
        val detector = OffRouteDetector(thresholdMeters = 35.0, graceMillis = 3_000)
        assertFalse(detector.update(50.0, timeMillis = 0))
        assertTrue(detector.update(50.0, timeMillis = 3_000))
    }
}
