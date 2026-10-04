package com.gamemaps.irl.audio.cue

/** Ce que l'application doit faire entendre : un son ou une phrase. */
sealed interface AudioCue {
    data class Sound(val effect: SoundEffect) : AudioCue
    data class Speech(val text: String) : AudioCue
}
