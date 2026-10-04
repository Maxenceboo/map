package com.gamemaps.irl.data.location

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

/** Filtres appliqués à chaque position : vitesse lissée et position figée à l'arrêt. */
class LocationFiltersTest {

    private fun fix(speedMps: Float?, position: LatLng = TestFixtures.START) =
        TestFixtures.fix(position).copy(speedMetersPerSecond = speedMps)

    @Test
    fun `vitesse forcée à 0 sous 3 km par heure`() {
        val smoother = SpeedSmoother()
        assertEquals(0f, smoother.smooth(fix(0.5f)).speedMetersPerSecond)
    }

    @Test
    fun `vitesse lissée - un pic isolé est amorti`() {
        val smoother = SpeedSmoother()
        assertEquals(10f, smoother.smooth(fix(10f)).speedMetersPerSecond)
        val afterSpike = smoother.smooth(fix(20f)).speedMetersPerSecond!!
        assertEquals(13.5f, afterSpike, 0.01f) // 10 × 0,65 + 20 × 0,35
    }

    @Test
    fun `vitesse inconnue laissée telle quelle`() {
        assertEquals(null, SpeedSmoother().smooth(fix(null)).speedMetersPerSecond)
    }

    @Test
    fun `à l'arrêt, le bruit GPS de quelques mètres est ignoré`() {
        val filter = StationaryPositionFilter()
        val anchor = filter.filter(fix(0f)).position
        val jitter = LatLng(TestFixtures.START.lat + 0.00002, TestFixtures.START.lng) // ~2 m
        assertEquals(anchor, filter.filter(fix(0f, jitter)).position)
    }

    @Test
    fun `en roulant, la position suit le GPS`() {
        val filter = StationaryPositionFilter()
        filter.filter(fix(0f))
        val moved = LatLng(TestFixtures.START.lat + 0.00002, TestFixtures.START.lng)
        assertEquals(moved, filter.filter(fix(5f, moved)).position)
    }
}
