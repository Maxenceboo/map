package com.gamemaps.irl.data.location

import com.gamemaps.irl.core.geo.LatLng

/**
 * Une mesure de position reçue du GPS.
 *
 * @property speedMetersPerSecond vitesse instantanée, null si le capteur ne la fournit pas.
 * @property bearingDegrees cap de déplacement [0, 360[, null si inconnu.
 * @property accuracyMeters rayon d'incertitude horizontale.
 */
data class GpsFix(
    val position: LatLng,
    val speedMetersPerSecond: Float?,
    val bearingDegrees: Float?,
    val accuracyMeters: Float,
    val timeMillis: Long,
) {
    val speedKmh: Int get() = ((speedMetersPerSecond ?: 0f) * 3.6f).toInt()
}
