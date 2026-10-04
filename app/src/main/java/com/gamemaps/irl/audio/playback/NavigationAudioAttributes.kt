package com.gamemaps.irl.audio.playback

import android.media.AudioAttributes

/**
 * Déclare nos sons comme "guidage de navigation" : Android baisse la musique pendant l'annonce,
 * et en Android Auto le son part vers les haut-parleurs de la voiture.
 */
object NavigationAudioAttributes {

    val SPEECH: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    val SOUND: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
}
