package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.navigation.radar.RadarAlert

/**
 * Annonce l'entrée dans une zone de danger : double bip + "Zone de danger".
 * Chaque zone n'est annoncée qu'une fois, et rien n'indique où se trouve le contrôle.
 */
class RadarAnnouncer {

    private var announcedZoneId: String? = null

    fun onAlert(alert: RadarAlert?): List<AudioCue> {
        if (alert == null || alert.zoneId == announcedZoneId) return emptyList()
        announcedZoneId = alert.zoneId
        return listOf(AudioCue.Sound(SoundEffect.RADAR_WARNING), AudioCue.Speech("Zone de danger"))
    }
}
