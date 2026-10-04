package com.gamemaps.irl.data.radar

import com.gamemaps.irl.core.geo.LatLng

/**
 * Un radar automatique.
 *
 * @property maxSpeedKmh vitesse contrôlée si elle est renseignée dans OpenStreetMap.
 */
data class Radar(
    val id: Long,
    val position: LatLng,
    val maxSpeedKmh: Int?,
)
