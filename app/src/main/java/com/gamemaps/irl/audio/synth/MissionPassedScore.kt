package com.gamemaps.irl.audio.synth

import kotlin.math.pow

/**
 * Fanfare d'arrivée façon GTA V "Mission Passed" (cahier des charges §7.1), ~3 secondes :
 *
 * 1. Accord de cuivres **suspendu mineur** (Ré – Sol – La), tendu, qui appelle une résolution.
 * 2. Résolution sur un accord **majeur triomphal** (La – Do# – Mi – La) qui s'éteint lentement.
 * 3. **Basse profonde** à 55 Hz (La grave) au moment de la résolution, pour l'impact physique.
 * 4. **Nappe de cordes** : deux oscillateurs désaccordés de ±7 cents → effet de chœur.
 *
 * Cuivres = dents de scie adoucies par un passe-bas ; cordes et basse = sinusoïdes.
 */
object MissionPassedScore {

    private const val D4 = 293.66
    private const val G4 = 392.00
    private const val A3 = 220.00
    private const val C_SHARP4 = 277.18
    private const val E4 = 329.63
    private const val A4 = 440.00
    private const val E5 = 659.25
    private const val A1 = 55.0

    private const val RESOLUTION_AT = 0.75
    private const val BRASS_CUTOFF_HZ = 1_800.0

    val tones: List<Tone> = suspendedChord() + majorChord() + subBass() + strings()

    private fun suspendedChord() = listOf(D4, G4, A4).map { note ->
        Tone(0.0, 0.8, note, waveform = Waveform.SAWTOOTH, gain = 0.07, lowpassHz = BRASS_CUTOFF_HZ)
    }

    private fun majorChord() = listOf(A3, C_SHARP4, E4, A4).map { note ->
        Tone(RESOLUTION_AT, 2.2, note, waveform = Waveform.SAWTOOTH, gain = 0.07, lowpassHz = BRASS_CUTOFF_HZ)
    }

    private fun subBass() = listOf(Tone(RESOLUTION_AT, 1.8, A1, gain = 0.35))

    private fun strings() = listOf(-7.0, 7.0).map { cents ->
        Tone(RESOLUTION_AT + 0.05, 2.3, detune(E5, cents), gain = 0.05)
    }

    /** Décale une fréquence de [cents] centièmes de demi-ton. */
    private fun detune(hz: Double, cents: Double): Double = hz * 2.0.pow(cents / 1_200)
}
