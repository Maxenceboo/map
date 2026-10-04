package com.gamemaps.irl.audio.playback

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Guidage vocal en français avec le moteur de synthèse vocale du téléphone.
 *
 * Le moteur met un peu de temps à s'initialiser : les phrases demandées avant sont gardées
 * puis prononcées dès qu'il est prêt. Les phrases s'enchaînent (pas d'interruption).
 */
class VoiceGuide(context: Context, private val focus: NavigationAudioFocus) {

    private lateinit var tts: TextToSpeech
    private var ready = false
    private val pending = mutableListOf<Pair<String, Long>>()
    private val utteranceCounter = AtomicInteger()

    init {
        tts = TextToSpeech(context.applicationContext, ::onInit)
    }

    /** Prononce [text], après [leadingSilenceMillis] de silence (pour laisser finir un carillon). */
    fun speak(text: String, leadingSilenceMillis: Long = 0) {
        if (!ready) {
            pending += text to leadingSilenceMillis
            return
        }
        if (leadingSilenceMillis > 0) {
            tts.playSilentUtterance(leadingSilenceMillis, TextToSpeech.QUEUE_ADD, SILENCE_ID)
        }
        focus.acquire()
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "$SPEECH_PREFIX${utteranceCounter.incrementAndGet()}")
    }

    private fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        tts.language = Locale.FRANCE
        tts.setAudioAttributes(NavigationAudioAttributes.SPEECH)
        tts.setSpeechRate(SPEECH_RATE)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = Unit
            override fun onDone(utteranceId: String) = releaseFocusFor(utteranceId)
            @Deprecated("Remplacée par onError(String, Int)")
            override fun onError(utteranceId: String) = releaseFocusFor(utteranceId)
        })
        ready = true
        pending.toList().forEach { (text, silence) -> speak(text, silence) }
        pending.clear()
    }

    /** Seules les vraies phrases ont pris le focus (pas les silences). */
    private fun releaseFocusFor(utteranceId: String) {
        if (utteranceId.startsWith(SPEECH_PREFIX)) focus.release()
    }

    private companion object {
        const val SPEECH_PREFIX = "say-"
        const val SILENCE_ID = "silence"
        const val SPEECH_RATE = 1.05f
    }
}
