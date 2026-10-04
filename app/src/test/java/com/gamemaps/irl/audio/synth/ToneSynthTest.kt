package com.gamemaps.irl.audio.synth

import com.gamemaps.irl.audio.cue.SoundEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ToneSynthTest {

    @Test
    fun `la durée rendue correspond à la fin de la dernière note`() {
        val pcm = ToneSynth.render(listOf(Tone(0.0, 0.1, 440.0), Tone(0.2, 0.1, 880.0)), sampleRate = 10_000)
        assertEquals(3_000, pcm.size) // 0,3 s à 10 kHz
    }

    @Test
    fun `aucune note donne un son vide`() {
        assertEquals(0, ToneSynth.render(emptyList()).size)
    }

    @Test
    fun `chaque effet produit un son audible et sans saturation brutale au démarrage`() {
        SoundEffect.entries.forEach { effect ->
            val pcm = ToneSynth.render(SoundEffectTones.tonesFor(effect))
            assertTrue("$effect est vide", pcm.isNotEmpty())
            assertTrue("$effect est silencieux", pcm.any { abs(it.toInt()) > 1_000 })
            assertEquals("$effect commence par un clic", 0, pcm[0].toInt()) // montée progressive
        }
    }

    @Test
    fun `les formes d'onde restent dans l'intervalle -1, 1`() {
        Waveform.entries.forEach { wave ->
            (0 until 100).map { wave.sample(it * 0.37) }.forEach { assertTrue(it in -1.0..1.0) }
        }
    }
}
