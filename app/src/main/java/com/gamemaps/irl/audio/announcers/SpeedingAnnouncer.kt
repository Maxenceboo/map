package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect

/**
 * Bip d'excès de vitesse (cahier des charges §6.3) : au moment où on dépasse la limite,
 * puis toutes les [repeatMillis] tant qu'on reste en excès. Discret, jamais en boucle.
 */
class SpeedingAnnouncer(private val repeatMillis: Long = 15_000) {

    private var wasSpeeding = false
    private var lastBeepMillis = 0L

    fun onSpeeding(isSpeeding: Boolean, nowMillis: Long): List<AudioCue> {
        val shouldBeep = isSpeeding && (!wasSpeeding || nowMillis - lastBeepMillis >= repeatMillis)
        wasSpeeding = isSpeeding
        if (!shouldBeep) return emptyList()
        lastBeepMillis = nowMillis
        return listOf(AudioCue.Sound(SoundEffect.SPEEDING))
    }
}
