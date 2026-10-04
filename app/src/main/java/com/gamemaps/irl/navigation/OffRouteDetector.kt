package com.gamemaps.irl.navigation

/**
 * Détecte une sortie d'itinéraire (cahier des charges §5.3) :
 * plus de [thresholdMeters] du tracé pendant plus de [graceMillis] consécutives.
 *
 * - Le délai de grâce évite de recalculer à cause d'un simple saut du GPS.
 * - À l'arrêt, on ne peut pas "sortir" de l'itinéraire : garé à 60 m de la route au départ,
 *   l'écart ne compte pas, sinon on recalculerait (et annoncerait le recalcul) en boucle.
 */
class OffRouteDetector(
    private val thresholdMeters: Double = 35.0,
    private val graceMillis: Long = 3_000,
    private val minSpeedMetersPerSecond: Float = 2f,
) {
    private var offRouteSince: Long? = null

    /** Renvoie true au moment où il faut déclencher un recalcul. */
    fun update(distanceFromRouteMeters: Double, timeMillis: Long, speedMetersPerSecond: Float? = null): Boolean {
        if (distanceFromRouteMeters <= thresholdMeters) {
            offRouteSince = null
            return false
        }
        if ((speedMetersPerSecond ?: Float.MAX_VALUE) < minSpeedMetersPerSecond) return false
        val since = offRouteSince ?: timeMillis.also { offRouteSince = it }
        return timeMillis - since >= graceMillis
    }

    fun reset() {
        offRouteSince = null
    }
}
