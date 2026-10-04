package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.navigation.radar.RadarAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarAnnouncerTest {

    private val announcer = RadarAnnouncer()
    private val radar = Radar(42, LatLng(44.86, -0.55), maxSpeedKmh = 50)

    @Test
    fun `avertissement puis urgence, une fois chacun`() {
        assertEquals(
            listOf(AudioCue.Sound(SoundEffect.RADAR_WARNING), AudioCue.Speech("Radar dans 600 mètres")),
            announcer.onAlert(RadarAlert(radar, 610.0)),
        )
        assertTrue(announcer.onAlert(RadarAlert(radar, 500.0)).isEmpty())
        assertTrue(announcer.onAlert(null).isEmpty()) // l'alerte clignote en bord de cône
        assertTrue(announcer.onAlert(RadarAlert(radar, 450.0)).isEmpty())

        assertEquals(listOf(AudioCue.Sound(SoundEffect.RADAR_URGENT)), announcer.onAlert(RadarAlert(radar, 290.0)))
        assertTrue(announcer.onAlert(RadarAlert(radar, 150.0)).isEmpty())
    }

    @Test
    fun `un radar détecté directement très près sonne en urgence`() {
        assertEquals(listOf(AudioCue.Sound(SoundEffect.RADAR_URGENT)), announcer.onAlert(RadarAlert(radar, 200.0)))
    }
}
