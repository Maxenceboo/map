package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.radar.Radar
import kotlin.math.abs

/**
 * Cahier des charges §6.2 : alerte si un radar est à moins de [rangeMeters] **devant**
 * le véhicule, c'est-à-dire dans un cône de ±[coneDegrees] autour du cap.
 * Sous [urgentMeters], l'alerte passe au niveau [RadarAlertLevel.URGENT].
 *
 * Sans cap connu (véhicule à l'arrêt depuis le lancement), pas d'alerte : on ne sait pas ce qui est "devant".
 */
class RadarAlertDetector(
    private val rangeMeters: Double = 800.0,
    private val urgentMeters: Double = 300.0,
    private val coneDegrees: Double = 30.0,
) {
    fun detect(fix: GpsFix, radars: List<Radar>): RadarAlert? {
        val heading = fix.bearingDegrees ?: return null
        val nearest = radars
            .map { it to GeoMath.distanceMeters(fix.position, it.position) }
            .filter { (_, distance) -> distance <= rangeMeters }
            .filter { (radar, _) -> angleBetween(heading.toDouble(), GeoMath.bearingDegrees(fix.position, radar.position)) <= coneDegrees }
            .minByOrNull { (_, distance) -> distance }
            ?: return null
        val (radar, distance) = nearest
        val level = if (distance <= urgentMeters) RadarAlertLevel.URGENT else RadarAlertLevel.WARNING
        return RadarAlert(radar, distance, level)
    }

    /** Écart angulaire le plus court entre deux caps, dans [0, 180]. */
    private fun angleBetween(a: Double, b: Double): Double {
        val diff = abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }
}
