package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedingAnnouncerTest {

    private val beep = listOf(AudioCue.Sound(SoundEffect.SPEEDING))

    @Test
    fun `bip au dépassement puis toutes les 15 secondes`() {
        val announcer = SpeedingAnnouncer(repeatMillis = 15_000)
        assertTrue(announcer.onSpeeding(false, 0).isEmpty())
        assertEquals(beep, announcer.onSpeeding(true, 1_000))
        assertTrue(announcer.onSpeeding(true, 10_000).isEmpty())
        assertEquals(beep, announcer.onSpeeding(true, 16_000))
    }

    @Test
    fun `revenir sous la limite puis redépasser rebipe tout de suite`() {
        val announcer = SpeedingAnnouncer(repeatMillis = 15_000)
        announcer.onSpeeding(true, 0)
        announcer.onSpeeding(false, 2_000)
        assertEquals(beep, announcer.onSpeeding(true, 3_000))
    }
}
