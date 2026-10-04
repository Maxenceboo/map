package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RadarAlertDetectorTest {

    private val detector = RadarAlertDetector()
    private val car = LatLng(44.8400, -0.5800)

    /** ~500 m au nord de la voiture. */
    private val radarNorth = Radar("r1", LatLng(44.8445, -0.5800), RadarType.SPEED, maxSpeedKmh = 50)

    /** ~300 m au sud (derrière quand on roule vers le nord). */
    private val radarSouth = Radar("r2", LatLng(44.8373, -0.5800), RadarType.RED_LIGHT, maxSpeedKmh = null)

    /** ~1,5 km au nord : trop loin. */
    private val radarFar = Radar("r3", LatLng(44.8535, -0.5800), RadarType.SPEED, maxSpeedKmh = 90)

    private fun drivingNorth() = TestFixtures.fix(car).copy(bearingDegrees = 0f)

    @Test
    fun `radar devant à moins de 800 m`() {
        val alert = detector.detect(drivingNorth(), listOf(radarNorth, radarFar))!!
        assertEquals("r1", alert.radar.id)
        assertEquals(500.0, alert.distanceMeters, 10.0)
    }

    @Test
    fun `un radar derrière ne déclenche rien`() {
        assertNull(detector.detect(drivingNorth(), listOf(radarSouth)))
    }

    @Test
    fun `en roulant vers le sud, c'est l'autre radar qui compte`() {
        val alert = detector.detect(drivingNorth().copy(bearingDegrees = 180f), listOf(radarNorth, radarSouth))!!
        assertEquals("r2", alert.radar.id)
    }

    @Test
    fun `cône de 30 degrés, y compris autour du nord`() {
        assertEquals("r1", detector.detect(drivingNorth().copy(bearingDegrees = 340f), listOf(radarNorth))?.radar?.id)
        assertNull(detector.detect(drivingNorth().copy(bearingDegrees = 45f), listOf(radarNorth)))
    }

    @Test
    fun `sans cap connu, pas d'alerte`() {
        assertNull(detector.detect(drivingNorth().copy(bearingDegrees = null), listOf(radarNorth)))
    }

    @Test
    fun `niveau urgent sous 300 m`() {
        assertEquals(RadarAlertLevel.WARNING, detector.detect(drivingNorth(), listOf(radarNorth))?.level)
        val close = radarNorth.copy(position = LatLng(44.8420, -0.5800)) // ~220 m
        assertEquals(RadarAlertLevel.URGENT, detector.detect(drivingNorth(), listOf(close))?.level)
    }
}
