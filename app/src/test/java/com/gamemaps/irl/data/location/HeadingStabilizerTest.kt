package com.gamemaps.irl.data.location

import com.gamemaps.irl.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeadingStabilizerTest {

    @Test
    fun `garde le dernier cap fiable à l'arrêt`() {
        val stabilizer = HeadingStabilizer()
        val moving = TestFixtures.fix(TestFixtures.START).copy(speedMetersPerSecond = 10f, bearingDegrees = 90f)
        val stopped = moving.copy(speedMetersPerSecond = 0.2f, bearingDegrees = 270f)

        assertEquals(90f, stabilizer.stabilize(moving).bearingDegrees)
        assertEquals(90f, stabilizer.stabilize(stopped).bearingDegrees)
    }

    @Test
    fun `aucun cap tant qu'on n'a pas roulé`() {
        val stopped = TestFixtures.fix(TestFixtures.START).copy(speedMetersPerSecond = 0f, bearingDegrees = 45f)
        assertNull(HeadingStabilizer().stabilize(stopped).bearingDegrees)
    }
}
