package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.data.radar.Radar

/** Un radar droit devant, à [distanceMeters]. */
data class RadarAlert(
    val radar: Radar,
    val distanceMeters: Double,
)
