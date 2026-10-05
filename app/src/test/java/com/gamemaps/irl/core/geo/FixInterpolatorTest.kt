package com.gamemaps.irl.core.geo

import com.gamemaps.irl.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class FixInterpolatorTest {

    private val a = TestFixtures.fix(LatLng(44.84, -0.58)).copy(bearingDegrees = 350f)
    private val b = TestFixtures.fix(LatLng(44.85, -0.56)).copy(bearingDegrees = 10f)

    @Test
    fun `à mi-chemin, la position est au milieu`() {
        val mid = FixInterpolator.between(a, b, 0.5f)
        assertEquals(44.845, mid.position.lat, 1e-9)
        assertEquals(-0.57, mid.position.lng, 1e-9)
    }

    @Test
    fun `le cap tourne par le plus court chemin`() {
        assertEquals(0f, FixInterpolator.between(a, b, 0.5f).bearingDegrees!!, 1e-3f) // 350° → 10° passe par 0°, pas par 180°
        assertEquals(350f, FixInterpolator.between(a, b, 0f).bearingDegrees!!, 1e-3f)
        assertEquals(10f, FixInterpolator.between(a, b, 1f).bearingDegrees!!, 1e-3f)
    }

    @Test
    fun `sans cap de départ, on prend celui d'arrivée`() {
        assertEquals(10f, FixInterpolator.between(a.copy(bearingDegrees = null), b, 0.3f).bearingDegrees!!, 1e-3f)
    }
}
