package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.navigation.radar.RadarAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarAnnouncerTest {

    private val announcer = RadarAnnouncer()
    private val zone = RadarAlert(zoneId = "r1", maxSpeedKmh = 80)

    @Test
    fun `entrée en zone - double bip et annonce, sans distance ni type`() {
        assertEquals(
            listOf(AudioCue.Sound(SoundEffect.RADAR_WARNING), AudioCue.Speech("Zone de danger")),
            announcer.onAlert(zone),
        )
    }

    @Test
    fun `une zone n'est annoncée qu'une fois`() {
        announcer.onAlert(zone)
        assertTrue(announcer.onAlert(zone).isEmpty())
        assertTrue(announcer.onAlert(null).isEmpty())
    }

    @Test
    fun `une autre zone est annoncée à son tour`() {
        announcer.onAlert(zone)
        assertEquals(2, announcer.onAlert(zone.copy(zoneId = "r2")).size)
    }
}
