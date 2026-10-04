package com.gamemaps.irl.car.alerts

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.radar.RadarType
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.navigation.radar.RadarAlertLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarRadarAlertPolicyTest {

    private val policy = CarRadarAlertPolicy()
    private val radar = Radar("fr_1", LatLng(44.86, -0.55), RadarType.RED_LIGHT, maxSpeedKmh = 50, road = "D1")

    private fun alert(distance: Double, level: RadarAlertLevel = RadarAlertLevel.WARNING) = RadarAlert(radar, distance, level)

    @Test
    fun `nouveau radar - alerte avec type, vitesse et distance`() {
        val decision = policy.onAlert(alert(610.0)) as CarRadarAlertPolicy.Decision.Show
        assertEquals("RADAR FEU ROUGE · 50 km/h", decision.title)
        assertEquals("dans 610 m", decision.subtitle)
    }

    @Test
    fun `pas de rafraîchissement à chaque mètre, mais réaffichage en urgence`() {
        policy.onAlert(alert(610.0))
        assertEquals(CarRadarAlertPolicy.Decision.Nothing, policy.onAlert(alert(500.0)))

        val urgent = policy.onAlert(alert(280.0, RadarAlertLevel.URGENT)) as CarRadarAlertPolicy.Decision.Show
        assertTrue(urgent.title.startsWith("⚠ "))
        assertEquals(CarRadarAlertPolicy.Decision.Nothing, policy.onAlert(alert(150.0, RadarAlertLevel.URGENT)))
    }

    @Test
    fun `radar dépassé - l'alerte est retirée une seule fois`() {
        val shown = policy.onAlert(alert(610.0)) as CarRadarAlertPolicy.Decision.Show
        assertEquals(CarRadarAlertPolicy.Decision.Dismiss(shown.alertId), policy.onAlert(null))
        assertEquals(CarRadarAlertPolicy.Decision.Nothing, policy.onAlert(null))
    }

    @Test
    fun `aucune alerte en cours - rien à retirer`() {
        assertEquals(CarRadarAlertPolicy.Decision.Nothing, policy.onAlert(null))
    }
}
