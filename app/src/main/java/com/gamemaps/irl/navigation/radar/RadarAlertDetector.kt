package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.radar.Radar
import kotlin.math.abs

/**
 * Détecte l'entrée dans une zone de danger et la sortie (cahier des charges §6.2, adapté à la loi française).
 *
 * Une zone s'étend sur [DangerZoneSize.lengthMeters] : les trois quarts avant le point de contrôle,
 * un quart après. On y entre quand le point est **devant** le véhicule (cône de ±[coneDegrees]) ;
 * on y reste jusqu'à en être sorti, pour que la fin de l'alerte ne trahisse pas l'emplacement exact.
 *
 * Sans cap connu (véhicule à l'arrêt depuis le lancement), pas d'entrée en zone : on ne sait pas ce qui est "devant".
 */
class RadarAlertDetector(private val coneDegrees: Double = 60.0) {

    /** Point de contrôle de la zone dans laquelle on se trouve. */
    private var active: Radar? = null

    fun detect(fix: GpsFix, radars: List<Radar>): RadarAlert? {
        active?.let { current ->
            if (stillInside(fix, current)) return alertFor(current)
            active = null
        }
        val heading = fix.bearingDegrees ?: return null
        val entered = radars
            .map { it to GeoMath.distanceMeters(fix.position, it.position) }
            .filter { (radar, distance) -> distance <= aheadMeters(radar) && isAhead(fix, heading, radar) }
            .minByOrNull { (_, distance) -> distance }
            ?.first
            ?: return null
        active = entered
        return alertFor(entered)
    }

    private fun stillInside(fix: GpsFix, radar: Radar): Boolean {
        val distance = GeoMath.distanceMeters(fix.position, radar.position)
        if (distance <= behindMeters(radar)) return true
        val heading = fix.bearingDegrees ?: return distance <= aheadMeters(radar)
        return distance <= aheadMeters(radar) && isAhead(fix, heading, radar)
    }

    private fun isAhead(fix: GpsFix, heading: Float, radar: Radar): Boolean =
        angleBetween(heading.toDouble(), GeoMath.bearingDegrees(fix.position, radar.position)) <= coneDegrees

    private fun aheadMeters(radar: Radar) = DangerZoneSize.lengthMeters(radar.maxSpeedKmh) * AHEAD_SHARE
    private fun behindMeters(radar: Radar) = DangerZoneSize.lengthMeters(radar.maxSpeedKmh) * (1 - AHEAD_SHARE)

    private fun alertFor(radar: Radar) = RadarAlert(zoneId = radar.id, maxSpeedKmh = radar.maxSpeedKmh)

    /** Écart angulaire le plus court entre deux caps, dans [0, 180]. */
    private fun angleBetween(a: Double, b: Double): Double {
        val diff = abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }

    private companion object {
        /** Part de la zone située avant le point de contrôle. */
        const val AHEAD_SHARE = 0.75
    }
}
