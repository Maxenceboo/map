package com.gamemaps.irl.audio.playback

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager

/**
 * Demande temporairement le "focus audio" pour que la musique baisse (ducking) pendant une annonce.
 *
 * Compteur de détenteurs : un son et une phrase peuvent se chevaucher, on ne rend le focus
 * que lorsque le dernier a fini.
 */
class NavigationAudioFocus(context: Context) {

    private val manager = context.getSystemService(AudioManager::class.java)
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(NavigationAudioAttributes.SPEECH)
        .build()
    private var holders = 0

    @Synchronized
    fun acquire() {
        if (holders++ == 0) manager.requestAudioFocus(request)
    }

    @Synchronized
    fun release() {
        if (holders == 0) return
        if (--holders == 0) manager.abandonAudioFocusRequest(request)
    }
}
