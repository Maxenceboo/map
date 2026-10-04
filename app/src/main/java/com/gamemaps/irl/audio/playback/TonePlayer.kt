package com.gamemaps.irl.audio.playback

import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.audio.synth.SoundEffectTones
import com.gamemaps.irl.audio.synth.ToneSynth

/**
 * Joue un [SoundEffect] : synthèse (mise en cache) puis lecture via un [AudioTrack] statique,
 * libéré automatiquement à la fin du son.
 */
class TonePlayer(private val focus: NavigationAudioFocus) {

    private val cache = mutableMapOf<SoundEffect, ShortArray>()
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Joue le son et renvoie sa durée en millisecondes. */
    fun play(effect: SoundEffect): Long {
        val pcm = cache.getOrPut(effect) { ToneSynth.render(SoundEffectTones.tonesFor(effect)) }
        if (pcm.isEmpty()) return 0
        val track = createTrack(pcm.size)
        track.write(pcm, 0, pcm.size)
        track.notificationMarkerPosition = pcm.size
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack) {
                track.release()
                focus.release()
            }

            override fun onPeriodicNotification(track: AudioTrack) = Unit
        }, mainHandler)
        focus.acquire()
        track.play()
        return pcm.size * 1_000L / ToneSynth.SAMPLE_RATE
    }

    private fun createTrack(sampleCount: Int): AudioTrack = AudioTrack.Builder()
        .setAudioAttributes(NavigationAudioAttributes.SOUND)
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(ToneSynth.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
        )
        .setBufferSizeInBytes(sampleCount * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
}
