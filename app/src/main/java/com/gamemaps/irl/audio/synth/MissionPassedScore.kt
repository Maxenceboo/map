package com.gamemaps.irl.audio.synth

/**
 * Fanfare d'arrivée, composée pour l'app (~2,8 secondes) :
 *
 * 1. Une **montée de quatre notes** (Do – Mi – Sol – Do) jouée comme une petite cloche.
 * 2. Un **accord tenu** (Do – Sol – Ré – Mi) aux cuivres doux, qui s'éteint lentement.
 * 3. Une **basse** grave (Do) au moment de l'accord, pour l'impact.
 * 4. Un **scintillement** aigu (Sol), deux sinusoïdes légèrement désaccordées.
 */
object MissionPassedScore {

    private const val C2 = 65.41
    private const val C4 = 261.63
    private const val E4 = 329.63
    private const val G4 = 392.00
    private const val C5 = 523.25
    private const val D5 = 587.33
    private const val E5 = 659.25
    private const val G5 = 783.99

    private const val NOTE_SPACING = 0.13
    private const val CHORD_AT = 0.55
    private const val BRASS_CUTOFF_HZ = 2_000.0

    val tones: List<Tone> = rise() + chord() + bass() + sparkle()

    private fun rise() = listOf(C4, E4, G4, C5).mapIndexed { i, note ->
        Tone(i * NOTE_SPACING, 0.3, note, waveform = Waveform.TRIANGLE, gain = 0.16)
    }

    private fun chord() = listOf(C4, G4, D5, E5).map { note ->
        Tone(CHORD_AT, 2.2, note, waveform = Waveform.SAWTOOTH, gain = 0.06, lowpassHz = BRASS_CUTOFF_HZ)
    }

    private fun bass() = listOf(Tone(CHORD_AT, 1.8, C2, gain = 0.3))

    private fun sparkle() = listOf(0.995, 1.005).map { ratio ->
        Tone(CHORD_AT + 0.08, 2.0, G5 * ratio, gain = 0.04)
    }
}
