package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.radar.Radar
import kotlin.math.abs

/**
 * Cahier des charges §6.2 : alerte si un radar est à moins de [rangeMeters] **devant**
 * le véhicule, c'est-à-dire dans un cône de ±[coneDegrees] autour du cap.
 *
 * Sans cap connu (véhicule à l'arrêt depuis le lancement), pas d'alerte : on ne sait pas ce qui est "devant".
 */
class RadarAlertDetector(
    private val rangeMeters: Double = 800.0,
    private val coneDegrees: Double = 30.0,
) {
    fun detect(fix: GpsFix, radars: List<Radar>): RadarAlert? {
        val heading = fix.bearingDegrees ?: return null
        return radars
            .map { RadarAlert(it, GeoMath.distanceMeters(fix.position, it.position)) }
            .filter { it.distanceMeters <= rangeMeters }
            .filter { angleBetween(heading.toDouble(), GeoMath.bearingDegrees(fix.position, it.radar.position)) <= coneDegrees }
            .minByOrNull { it.distanceMeters }
    }

    /** Écart angulaire le plus court entre deux caps, dans [0, 180]. */
    private fun angleBetween(a: Double, b: Double): Double {
        val diff = abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }
}
