package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.radar.RadarType
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.navigation.radar.RadarAlertLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarAnnouncerTest {

    private val announcer = RadarAnnouncer()
    private val radar = Radar("fr_1", LatLng(44.86, -0.55), RadarType.SPEED, maxSpeedKmh = 50)

    private fun warning(distance: Double) = RadarAlert(radar, distance, RadarAlertLevel.WARNING)
    private fun urgent(distance: Double) = RadarAlert(radar, distance, RadarAlertLevel.URGENT)

    @Test
    fun `avertissement puis urgence, une fois chacun`() {
        assertEquals(
            listOf(AudioCue.Sound(SoundEffect.RADAR_WARNING), AudioCue.Speech("Radar dans 600 mètres")),
            announcer.onAlert(warning(610.0)),
        )
        assertTrue(announcer.onAlert(warning(500.0)).isEmpty())
        assertTrue(announcer.onAlert(null).isEmpty()) // l'alerte clignote en bord de cône
        assertTrue(announcer.onAlert(warning(450.0)).isEmpty())

        assertEquals(listOf(AudioCue.Sound(SoundEffect.RADAR_URGENT)), announcer.onAlert(urgent(290.0)))
        assertTrue(announcer.onAlert(urgent(150.0)).isEmpty())
    }

    @Test
    fun `un radar détecté directement très près sonne en urgence`() {
        assertEquals(listOf(AudioCue.Sound(SoundEffect.RADAR_URGENT)), announcer.onAlert(urgent(200.0)))
    }

    @Test
    fun `le type de radar est annoncé`() {
        val redLight = RadarAlert(radar.copy(id = "fr_2", type = RadarType.RED_LIGHT), 500.0)
        assertEquals(AudioCue.Speech("Radar feu rouge dans 500 mètres"), announcer.onAlert(redLight)[1])
    }
}
