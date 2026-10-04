package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.data.radar.Radar

/** Un radar droit devant, à [distanceMeters], avec son niveau d'alerte. */
data class RadarAlert(
    val radar: Radar,
    val distanceMeters: Double,
    val level: RadarAlertLevel = RadarAlertLevel.WARNING,
)
