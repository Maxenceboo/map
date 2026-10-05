package com.gamemaps.irl.car.alerts

import com.gamemaps.irl.car.alerts.CarRadarAlertPolicy.Decision
import com.gamemaps.irl.navigation.radar.RadarAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarRadarAlertPolicyTest {

    private val policy = CarRadarAlertPolicy()
    private val zone = RadarAlert(zoneId = "r1", maxSpeedKmh = 80)

    @Test
    fun `entrée en zone - on affiche, avec la vitesse autorisée`() {
        val shown = policy.onAlert(zone) as Decision.Show
        assertEquals("Zone de danger", shown.title)
        assertEquals("Limitée à 80 km/h", shown.subtitle)
    }

    @Test
    fun `vitesse inconnue - simple appel à la vigilance`() {
        val shown = policy.onAlert(zone.copy(maxSpeedKmh = null)) as Decision.Show
        assertEquals("Restez vigilant", shown.subtitle)
    }

    @Test
    fun `tant qu'on reste dans la zone, on ne réaffiche rien`() {
        policy.onAlert(zone)
        assertEquals(Decision.Nothing, policy.onAlert(zone))
    }

    @Test
    fun `sortie de zone - on retire l'alerte, une seule fois`() {
        val shown = policy.onAlert(zone) as Decision.Show
        assertEquals(Decision.Dismiss(shown.alertId), policy.onAlert(null))
        assertEquals(Decision.Nothing, policy.onAlert(null))
        assertTrue(policy.onAlert(zone) is Decision.Show)
    }
}
