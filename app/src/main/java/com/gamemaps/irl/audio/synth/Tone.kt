package com.gamemaps.irl.audio.synth

/**
 * Une note : elle démarre à [startSeconds], dure [durationSeconds], glisse de [startHz] à [endHz]
 * et s'éteint progressivement (décroissance exponentielle depuis [gain]).
 */
data class Tone(
    val startSeconds: Double,
    val durationSeconds: Double,
    val startHz: Double,
    val endHz: Double = startHz,
    val waveform: Waveform = Waveform.SINE,
    val gain: Double = 0.2,
) {
    val endSeconds: Double get() = startSeconds + durationSeconds
}
