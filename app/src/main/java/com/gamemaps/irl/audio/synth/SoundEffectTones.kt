package com.gamemaps.irl.audio.synth

import com.gamemaps.irl.audio.cue.SoundEffect

/**
 * Partition de chaque son, reprise de `services/audio.ts` de la version WebGL
 * (mêmes notes, durées et formes d'onde).
 */
object SoundEffectTones {

    private const val C5 = 523.25
    private const val E5 = 659.25
    private const val G5 = 783.99
    private const val A5 = 880.0
    private const val B5 = 987.77
    private const val C6 = 1046.5
    private const val E6 = 1318.51

    fun tonesFor(effect: SoundEffect): List<Tone> = when (effect) {
        SoundEffect.START -> listOf(
            Tone(0.0, 0.15, C5),
            Tone(0.12, 0.2, E5),
        )
        SoundEffect.TURN -> listOf(
            Tone(0.0, 0.1, A5),
            Tone(0.08, 0.18, C6),
        )
        SoundEffect.ARRIVAL -> listOf(C5, E5, G5, C6).mapIndexed { i, note ->
            Tone(startSeconds = i * 0.15, durationSeconds = 0.3, startHz = note, gain = 0.25)
        }
        SoundEffect.RADAR_WARNING -> listOf(0.0, 0.15).map { start ->
            Tone(start, 0.1, startHz = G5, endHz = C6, waveform = Waveform.TRIANGLE, gain = 0.18)
        }
        SoundEffect.RADAR_URGENT -> listOf(0.0, 0.12, 0.24).map { start ->
            Tone(start, 0.1, startHz = B5, endHz = E6, waveform = Waveform.SAWTOOTH, gain = 0.25)
        }
        SoundEffect.SPEEDING -> listOf(0.0, 0.14).map { start ->
            Tone(start, 0.08, startHz = A5, gain = 0.15)
        }
    }
}
