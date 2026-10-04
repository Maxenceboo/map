package com.gamemaps.irl.audio.synth

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Forme d'onde d'un oscillateur. [sample] prend une phase en radians et renvoie une valeur dans [-1, 1]. */
enum class Waveform {
    /** Son pur et doux (carillons). */
    SINE,

    /** Un peu plus brillant (bip radar normal). */
    TRIANGLE,

    /** Riche et agressif (bip radar urgent). */
    SAWTOOTH;

    fun sample(phase: Double): Double {
        val cycle = (phase / (2 * PI)) % 1.0
        return when (this) {
            SINE -> sin(phase)
            TRIANGLE -> 4 * abs(cycle - 0.5) - 1
            SAWTOOTH -> 2 * cycle - 1
        }
    }
}
