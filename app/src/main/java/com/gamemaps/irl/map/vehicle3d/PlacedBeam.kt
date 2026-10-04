package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.core.geo.LatLng

/** Un morceau de lumière posé sur la carte : contour en coordonnées GPS, hauteurs mises à l'échelle. */
data class PlacedBeam(
    val ring: List<LatLng>,
    val baseMeters: Double,
    val topMeters: Double,
    val opacity: Double,
)
