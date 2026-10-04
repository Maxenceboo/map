package com.gamemaps.irl.audio

import com.gamemaps.irl.audio.announcers.GuidanceAnnouncer
import com.gamemaps.irl.audio.announcers.RadarAnnouncer
import com.gamemaps.irl.audio.announcers.SpeedingAnnouncer
import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.playback.AudioOutput
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.SpeedingDetector
import com.gamemaps.irl.navigation.radar.RadarAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Chef d'orchestre audio, unique pour toute l'app (téléphone et Android Auto).
 *
 * Écoute le guidage, les radars et la vitesse, demande aux "announcers" quoi dire,
 * et l'envoie à la sortie audio — sauf si le son est coupé.
 */
class AudioController(
    private val scope: CoroutineScope,
    private val navigation: Flow<NavigationState>,
    private val radarAlerts: Flow<RadarAlert?>,
    private val fixes: Flow<GpsFix?>,
    private val speedLimits: Flow<Int?>,
    private val muted: StateFlow<Boolean>,
    private val output: AudioOutput,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val guidance = GuidanceAnnouncer()
    private val radars = RadarAnnouncer()
    private val speeding = SpeedingAnnouncer()

    fun start() {
        // Les announcers tournent même son coupé : ainsi, en réactivant le son,
        // on n'entend pas d'un coup toutes les annonces "en retard".
        scope.launch { navigation.collect { play(guidance.onState(it)) } }
        scope.launch { radarAlerts.collect { play(radars.onAlert(it)) } }
        scope.launch {
            combine(fixes, speedLimits) { fix, limit -> SpeedingDetector.isSpeeding(fix?.speedKmh ?: 0, limit) }
                .collect { play(speeding.onSpeeding(it, clock())) }
        }
    }

    private fun play(cues: List<AudioCue>) {
        if (cues.isNotEmpty() && !muted.value) output.play(cues)
    }
}
