package com.gamemaps.irl.navigation.snap

import com.gamemaps.irl.core.geo.LatLng

/**
 * Point d'une route sur lequel on peut poser le véhicule.
 *
 * @property position point de la route le plus proche de la position GPS.
 * @property bearingDegrees direction de la route à cet endroit, dans le sens de la marche.
 */
data class SnapCandidate(val position: LatLng, val bearingDegrees: Double)
