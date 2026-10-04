package com.gamemaps.irl.data.location

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng

/**
 * À l'arrêt, le GPS "bruite" de quelques mètres : la flèche gigoterait sur place.
 * Tant que la vitesse est nulle et que la position bouge de moins de [jitterMeters],
 * on garde la position précédente.
 */
class StationaryPositionFilter(private val jitterMeters: Double = 3.5) {

    private var anchor: LatLng? = null

    fun filter(fix: GpsFix): GpsFix {
        val previous = anchor
        val stopped = (fix.speedMetersPerSecond ?: 0f) == 0f
        if (stopped && previous != null && GeoMath.distanceMeters(previous, fix.position) < jitterMeters) {
            return fix.copy(position = previous)
        }
        anchor = fix.position
        return fix
    }
}
