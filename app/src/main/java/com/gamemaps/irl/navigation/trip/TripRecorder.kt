package com.gamemaps.irl.navigation.trip

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng

/**
 * Mesure le trajet réellement effectué pendant le guidage : distance parcourue et durée.
 *
 * Un déplacement qui impliquerait plus de [maxSpeedMetersPerSecond] (~250 km/h) entre deux mesures
 * est un "téléport" du GPS (perte de signal, tunnel) : il n'est pas compté, pour ne pas gonfler la distance.
 */
class TripRecorder(private val maxSpeedMetersPerSecond: Double = 70.0) {

    private var startMillis: Long? = null
    private var lastPosition: LatLng? = null
    private var lastMillis: Long = 0
    private var distanceMeters = 0.0

    fun start(nowMillis: Long, position: LatLng?) {
        startMillis = nowMillis
        lastPosition = position
        lastMillis = nowMillis
        distanceMeters = 0.0
    }

    fun onPosition(position: LatLng, timeMillis: Long) {
        if (startMillis == null) return
        lastPosition?.let { previous ->
            val step = GeoMath.distanceMeters(previous, position)
            val seconds = (timeMillis - lastMillis) / 1_000.0
            val plausible = seconds <= 0 || step / seconds <= maxSpeedMetersPerSecond
            if (plausible) distanceMeters += step
        }
        lastPosition = position
        lastMillis = timeMillis
    }

    fun finish(nowMillis: Long): TripStats {
        val start = startMillis ?: nowMillis
        startMillis = null
        return TripStats(distanceMeters = distanceMeters, durationSeconds = (nowMillis - start) / 1_000.0)
    }
}
