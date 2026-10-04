package com.gamemaps.irl.ui.screen

import com.gamemaps.irl.data.speedlimit.SpeedLimit
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.GpsQuality
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.navigation.radar.RadarAlert

/** Ce qui concerne la conduite, avec ou sans guidage : position, limitation, radars. */
data class DrivingState(
    val fix: GpsFix? = null,
    val speedLimit: SpeedLimit? = null,
    val radars: List<Radar> = emptyList(),
    val radarAlert: RadarAlert? = null,
) {
    val gpsQuality: GpsQuality get() = GpsQuality.from(fix)
}
