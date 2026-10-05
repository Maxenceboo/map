package com.gamemaps.irl.core.geo

import com.gamemaps.irl.data.location.GpsFix

/** Position intermédiaire entre deux mesures GPS, pour faire glisser le véhicule au lieu de le faire sauter. */
object FixInterpolator {

    /** @param fraction 0 = [from], 1 = [to]. Le cap tourne par le plus court chemin (350° → 10° passe par 0°). */
    fun between(from: GpsFix, to: GpsFix, fraction: Float): GpsFix {
        val t = fraction.coerceIn(0f, 1f).toDouble()
        return to.copy(
            position = LatLng(
                lat = from.position.lat + (to.position.lat - from.position.lat) * t,
                lng = from.position.lng + (to.position.lng - from.position.lng) * t,
            ),
            bearingDegrees = bearing(from.bearingDegrees, to.bearingDegrees, t),
        )
    }

    private fun bearing(from: Float?, to: Float?, t: Double): Float? {
        if (from == null || to == null) return to
        val delta = ((to - from + 540) % 360) - 180 // écart signé dans [-180, 180[
        return (((from + delta * t) % 360 + 360) % 360).toFloat()
    }
}
