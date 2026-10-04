package com.gamemaps.irl.audio.playback

import com.gamemaps.irl.audio.cue.AudioCue

/** Sortie audio : joue une liste de [AudioCue] dans l'ordre. Interface pour pouvoir la simuler. */
fun interface AudioOutput {
    fun play(cues: List<AudioCue>)
}
