package com.gamemaps.irl.audio.synth

/**
 * Une note : elle démarre à [startSeconds], dure [durationSeconds], glisse de [startHz] à [endHz]
 * et s'éteint progressivement (décroissance exponentielle depuis [gain]).
 *
 * @property lowpassHz fréquence de coupure d'un filtre passe-bas (adoucit une dent de scie
 * en son de cuivre) ; null = pas de filtre.
 */
data class Tone(
    val startSeconds: Double,
    val durationSeconds: Double,
    val startHz: Double,
    val endHz: Double = startHz,
    val waveform: Waveform = Waveform.SINE,
    val gain: Double = 0.2,
    val lowpassHz: Double? = null,
) {
    val endSeconds: Double get() = startSeconds + durationSeconds
}
