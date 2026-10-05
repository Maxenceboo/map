package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RadarAlertDetectorTest {

    private val detector = RadarAlertDetector()

    /** Point de contrôle sur une route à 80 : zone de 2 km (1,5 km avant, 500 m après). */
    private val control = Radar("r1", LatLng(44.8600, -0.5800), RadarType.SPEED, maxSpeedKmh = 80)

    /** Position à [meters] au sud du point de contrôle (négatif = au nord, donc après l'avoir passé). */
    private fun southOf(meters: Double) = LatLng(control.position.lat - meters / 111_320.0, control.position.lng)

    private fun drivingNorthAt(position: LatLng) = TestFixtures.fix(position).copy(bearingDegrees = 0f)

    @Test
    fun `taille de la zone selon la route`() {
        assertEquals(4_000.0, DangerZoneSize.lengthMeters(130), 0.0)
        assertEquals(2_000.0, DangerZoneSize.lengthMeters(80), 0.0)
        assertEquals(300.0, DangerZoneSize.lengthMeters(50), 0.0)
        assertEquals(2_000.0, DangerZoneSize.lengthMeters(null), 0.0)
    }

    @Test
    fun `on entre dans la zone bien avant le point de contrôle`() {
        assertNull(detector.detect(drivingNorthAt(southOf(1_800.0)), listOf(control)))
        val alert = detector.detect(drivingNorthAt(southOf(1_400.0)), listOf(control))
        assertEquals(RadarAlert(zoneId = "r1", maxSpeedKmh = 80), alert)
    }

    @Test
    fun `la zone continue après le point de contrôle, puis se termine`() {
        detector.detect(drivingNorthAt(southOf(1_000.0)), listOf(control))
        // 300 m après le point : toujours dans la zone, la fin de l'alerte ne trahit pas l'emplacement.
        assertNotNull(detector.detect(drivingNorthAt(southOf(-300.0)), listOf(control)))
        assertNull(detector.detect(drivingNorthAt(southOf(-700.0)), listOf(control)))
    }

    @Test
    fun `un point de contrôle derrière soi ne déclenche rien`() {
        assertNull(detector.detect(drivingNorthAt(southOf(-200.0)), listOf(control)))
    }

    @Test
    fun `sans cap connu, pas d'entrée en zone`() {
        val stopped = TestFixtures.fix(southOf(500.0)).copy(bearingDegrees = null)
        assertNull(detector.detect(stopped, listOf(control)))
    }
}
