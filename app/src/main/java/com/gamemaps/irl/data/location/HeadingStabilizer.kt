package com.gamemaps.irl.data.location

/**
 * Évite que le véhicule tourne sur lui-même à l'arrêt.
 *
 * À basse vitesse, le cap GPS est du bruit : on garde alors le dernier cap fiable.
 */
class HeadingStabilizer(private val minSpeedMetersPerSecond: Float = 1.5f) {

    private var lastReliableBearing: Float? = null

    fun stabilize(fix: GpsFix): GpsFix {
        val speed = fix.speedMetersPerSecond ?: 0f
        val bearing = fix.bearingDegrees
        if (bearing != null && speed >= minSpeedMetersPerSecond) {
            lastReliableBearing = bearing
            return fix
        }
        return fix.copy(bearingDegrees = lastReliableBearing)
    }
}
