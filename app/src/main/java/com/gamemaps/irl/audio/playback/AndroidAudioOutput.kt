package com.gamemaps.irl.audio.playback

import com.gamemaps.irl.audio.cue.AudioCue

/**
 * Joue sons et phrases sur le téléphone (et donc dans la voiture via Android Auto).
 * Une phrase qui suit un son attend la fin de celui-ci, pour ne pas parler par-dessus le carillon.
 */
class AndroidAudioOutput(
    private val tones: TonePlayer,
    private val voice: VoiceGuide,
) : AudioOutput {

    override fun play(cues: List<AudioCue>) {
        var soundMillis = 0L
        cues.forEach { cue ->
            when (cue) {
                is AudioCue.Sound -> soundMillis = maxOf(soundMillis, tones.play(cue.effect))
                is AudioCue.Speech -> {
                    voice.speak(cue.text, leadingSilenceMillis = soundMillis)
                    soundMillis = 0
                }
            }
        }
    }
}
