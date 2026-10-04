package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.navigation.radar.RadarAlertLevel
import com.gamemaps.irl.navigation.radar.RadarTypeText

/**
 * Bips radar en deux niveaux (comme la version WebGL) :
 * - dès qu'un nouveau radar est détecté : double bip + "Radar feu rouge dans 600 mètres" ;
 * - au passage en niveau urgent (< 300 m) : triple bip.
 *
 * Chaque niveau ne sonne qu'une fois par radar, même si l'alerte clignote en bord de cône.
 */
class RadarAnnouncer {

    private var warnedRadarId: String? = null
    private var urgentRadarId: String? = null

    fun onAlert(alert: RadarAlert?): List<AudioCue> {
        if (alert == null) return emptyList()
        val id = alert.radar.id
        return when {
            alert.level == RadarAlertLevel.URGENT && urgentRadarId != id -> {
                urgentRadarId = id
                warnedRadarId = id
                listOf(AudioCue.Sound(SoundEffect.RADAR_URGENT))
            }
            warnedRadarId != id -> {
                warnedRadarId = id
                val name = RadarTypeText.spoken(alert.radar.type)
                listOf(
                    AudioCue.Sound(SoundEffect.RADAR_WARNING),
                    AudioCue.Speech("$name dans ${SpeechDistanceFormatter.format(alert.distanceMeters)}"),
                )
            }
            else -> emptyList()
        }
    }
}
