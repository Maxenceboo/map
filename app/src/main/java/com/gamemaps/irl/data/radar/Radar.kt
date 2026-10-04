package com.gamemaps.irl.data.radar

import com.gamemaps.irl.core.geo.LatLng

/**
 * Un radar automatique.
 *
 * @property id unique toutes sources confondues : "fr_12923" (base officielle) ou "osm_42" (OpenStreetMap).
 * @property maxSpeedKmh vitesse contrôlée, si connue.
 * @property road route concernée ("A10"), si connue.
 */
data class Radar(
    val id: String,
    val position: LatLng,
    val type: RadarType,
    val maxSpeedKmh: Int?,
    val road: String? = null,
)
