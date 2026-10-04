package com.gamemaps.irl.audio.synth

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.pow

/**
 * Synthétiseur : transforme une liste de [Tone] en échantillons PCM 16 bits mono.
 * Équivalent natif des oscillateurs Web Audio de la version WebGL — aucun fichier audio.
 */
object ToneSynth {

    const val SAMPLE_RATE = 44_100

    /** Les gains Web Audio d'origine sont faibles : on les amplifie un peu pour le haut-parleur. */
    private const val MASTER_GAIN = 2.0

    /** Montée de 5 ms en début de note pour éviter un "clic". */
    private const val ATTACK_SECONDS = 0.005

    /** Niveau atteint en fin de note (comme `exponentialRampToValueAtTime(0.01)`). */
    private const val DECAY_FLOOR = 0.01

    fun render(tones: List<Tone>, sampleRate: Int = SAMPLE_RATE): ShortArray {
        val totalSeconds = tones.maxOfOrNull { it.endSeconds } ?: return ShortArray(0)
        val mix = DoubleArray((totalSeconds * sampleRate).roundToInt())
        tones.forEach { addTone(mix, it, sampleRate) }
        return ShortArray(mix.size) { i -> (mix[i] * MASTER_GAIN).coerceIn(-1.0, 1.0).times(Short.MAX_VALUE).toInt().toShort() }
    }

    private fun addTone(mix: DoubleArray, tone: Tone, sampleRate: Int) {
        val first = (tone.startSeconds * sampleRate).toInt()
        val count = (tone.durationSeconds * sampleRate).toInt()
        var phase = 0.0
        for (n in 0 until count) {
            val index = first + n
            if (index >= mix.size) break
            val progress = n.toDouble() / count
            // Glissement exponentiel de fréquence, comme exponentialRampToValueAtTime.
            val frequency = tone.startHz * (tone.endHz / tone.startHz).pow(progress)
            phase += 2 * PI * frequency / sampleRate
            mix[index] += tone.waveform.sample(phase) * tone.gain * envelope(n.toDouble() / sampleRate, progress)
        }
    }

    private fun envelope(timeSeconds: Double, progress: Double): Double {
        val attack = (timeSeconds / ATTACK_SECONDS).coerceAtMost(1.0)
        return attack * DECAY_FLOOR.pow(progress)
    }
}
