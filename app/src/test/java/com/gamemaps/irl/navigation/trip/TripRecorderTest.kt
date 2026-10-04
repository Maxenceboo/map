package com.gamemaps.irl.navigation.trip

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class TripRecorderTest {

    @Test
    fun `distance cumulée et durée`() {
        val recorder = TripRecorder()
        recorder.start(nowMillis = 0, position = TestFixtures.START)
        val middle = LatLng((TestFixtures.START.lat + TestFixtures.CORNER.lat) / 2, TestFixtures.START.lng)
        recorder.onPosition(middle, timeMillis = 60_000)
        recorder.onPosition(TestFixtures.CORNER, timeMillis = 120_000)

        val stats = recorder.finish(nowMillis = 120_000)
        assertEquals(GeoMath.distanceMeters(TestFixtures.START, TestFixtures.CORNER), stats.distanceMeters, 1.0)
        assertEquals(120.0, stats.durationSeconds, 0.0)
        assertEquals(30.0, stats.averageSpeedKmh, 1.0) // ~1 km en 2 min
    }

    @Test
    fun `un saut GPS aberrant n'est pas compté`() {
        val recorder = TripRecorder()
        recorder.start(nowMillis = 0, position = TestFixtures.START)
        recorder.onPosition(LatLng(45.5, 0.5), timeMillis = 1_000) // ~100 km en 1 s : perte de signal
        assertEquals(0.0, recorder.finish(nowMillis = 1_000).distanceMeters, 0.0)
    }

    @Test
    fun `rien n'est enregistré avant le départ`() {
        val recorder = TripRecorder()
        recorder.onPosition(TestFixtures.START, timeMillis = 0)
        recorder.onPosition(TestFixtures.CORNER, timeMillis = 60_000)
        assertEquals(0.0, recorder.finish(nowMillis = 0).distanceMeters, 0.0)
    }
}
