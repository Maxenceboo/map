package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RadarAlertDetectorTest {

    private val detector = RadarAlertDetector()
    private val car = LatLng(44.8400, -0.5800)

    /** ~500 m au nord de la voiture. */
    private val radarNorth = Radar(1, LatLng(44.8445, -0.5800), maxSpeedKmh = 50)

    /** ~300 m au sud (derrière quand on roule vers le nord). */
    private val radarSouth = Radar(2, LatLng(44.8373, -0.5800), maxSpeedKmh = null)

    /** ~1,5 km au nord : trop loin. */
    private val radarFar = Radar(3, LatLng(44.8535, -0.5800), maxSpeedKmh = 90)

    private fun drivingNorth() = TestFixtures.fix(car).copy(bearingDegrees = 0f)

    @Test
    fun `radar devant à moins de 800 m`() {
        val alert = detector.detect(drivingNorth(), listOf(radarNorth, radarFar))!!
        assertEquals(1L, alert.radar.id)
        assertEquals(500.0, alert.distanceMeters, 10.0)
    }

    @Test
    fun `un radar derrière ne déclenche rien`() {
        assertNull(detector.detect(drivingNorth(), listOf(radarSouth)))
    }

    @Test
    fun `en roulant vers le sud, c'est l'autre radar qui compte`() {
        val alert = detector.detect(drivingNorth().copy(bearingDegrees = 180f), listOf(radarNorth, radarSouth))!!
        assertEquals(2L, alert.radar.id)
    }

    @Test
    fun `cône de 30 degrés, y compris autour du nord`() {
        assertEquals(1L, detector.detect(drivingNorth().copy(bearingDegrees = 340f), listOf(radarNorth))?.radar?.id)
        assertNull(detector.detect(drivingNorth().copy(bearingDegrees = 45f), listOf(radarNorth)))
    }

    @Test
    fun `sans cap connu, pas d'alerte`() {
        assertNull(detector.detect(drivingNorth().copy(bearingDegrees = null), listOf(radarNorth)))
    }
}
