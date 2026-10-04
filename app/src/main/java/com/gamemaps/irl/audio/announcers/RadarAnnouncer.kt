package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.navigation.radar.RadarAlert

/**
 * Bips radar en deux niveaux (comme la version WebGL) :
 * - dès qu'un nouveau radar est détecté : double bip + "Radar dans 600 mètres" ;
 * - sous [urgentMeters] : triple bip urgent.
 *
 * Chaque niveau ne sonne qu'une fois par radar, même si l'alerte clignote en bord de cône.
 */
class RadarAnnouncer(private val urgentMeters: Double = 300.0) {

    private var warnedRadarId: Long? = null
    private var urgentRadarId: Long? = null

    fun onAlert(alert: RadarAlert?): List<AudioCue> {
        if (alert == null) return emptyList()
        val id = alert.radar.id
        return when {
            alert.distanceMeters <= urgentMeters && urgentRadarId != id -> {
                urgentRadarId = id
                warnedRadarId = id
                listOf(AudioCue.Sound(SoundEffect.RADAR_URGENT))
            }
            warnedRadarId != id -> {
                warnedRadarId = id
                listOf(
                    AudioCue.Sound(SoundEffect.RADAR_WARNING),
                    AudioCue.Speech("Radar dans ${SpeechDistanceFormatter.format(alert.distanceMeters)}"),
                )
            }
            else -> emptyList()
        }
    }
}
