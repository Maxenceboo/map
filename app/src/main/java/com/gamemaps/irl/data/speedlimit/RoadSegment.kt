package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.LatLng

/** Une route OpenStreetMap (un "way") avec sa vitesse maximale autorisée. */
data class RoadSegment(
    val wayId: Long,
    val maxSpeedKmh: Int,
    val points: List<LatLng>,
)
