package com.gamemaps.irl.audio

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.data.settings.AppSettings

/** Retire des annonces ce que l'utilisateur a désactivé dans les Paramètres. */
object AudioCueFilter {

    private val RADAR_SOUNDS = setOf(SoundEffect.RADAR_WARNING, SoundEffect.RADAR_URGENT)

    fun filter(cues: List<AudioCue>, settings: AppSettings): List<AudioCue> = cues.filter { cue ->
        when (cue) {
            is AudioCue.Speech -> settings.voiceGuidance
            is AudioCue.Sound -> when (cue.effect) {
                in RADAR_SOUNDS -> settings.radarBeeps
                SoundEffect.SPEEDING -> settings.speedingBeep
                else -> true // Carillons de départ, de virage et d'arrivée : toujours.
            }
        }
    }
}
