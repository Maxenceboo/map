package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.core.geo.LatLng

/** Une pièce posée sur la carte : contour en coordonnées GPS, hauteurs mises à l'échelle, couleur finale. */
data class PlacedPart(
    val ring: List<LatLng>,
    val baseMeters: Double,
    val topMeters: Double,
    val colorHex: String,
)
