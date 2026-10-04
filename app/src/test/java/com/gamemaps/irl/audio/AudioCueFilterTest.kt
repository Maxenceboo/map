package com.gamemaps.irl.audio

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.data.settings.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioCueFilterTest {

    private val turn = listOf(AudioCue.Sound(SoundEffect.TURN), AudioCue.Speech("Tournez à droite"))
    private val radar = listOf(AudioCue.Sound(SoundEffect.RADAR_WARNING), AudioCue.Speech("Radar dans 600 mètres"))
    private val speeding = listOf(AudioCue.Sound(SoundEffect.SPEEDING))

    @Test
    fun `tout est joué par défaut`() {
        val settings = AppSettings()
        assertEquals(turn, AudioCueFilter.filter(turn, settings))
        assertEquals(radar, AudioCueFilter.filter(radar, settings))
        assertEquals(speeding, AudioCueFilter.filter(speeding, settings))
    }

    @Test
    fun `sans guidage vocal - les carillons restent, les phrases disparaissent`() {
        val settings = AppSettings(voiceGuidance = false)
        assertEquals(listOf(AudioCue.Sound(SoundEffect.TURN)), AudioCueFilter.filter(turn, settings))
    }

    @Test
    fun `bips radar et excès de vitesse désactivables séparément`() {
        assertEquals(listOf(radar[1]), AudioCueFilter.filter(radar, AppSettings(radarBeeps = false)))
        assertEquals(emptyList<AudioCue>(), AudioCueFilter.filter(speeding, AppSettings(speedingBeep = false)))
        assertEquals(speeding, AudioCueFilter.filter(speeding, AppSettings(radarBeeps = false)))
    }
}
