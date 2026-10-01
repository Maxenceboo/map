package com.gamemaps.irl.navigation

/**
 * Détecte une sortie d'itinéraire (cahier des charges §5.3) :
 * plus de [thresholdMeters] du tracé pendant plus de [graceMillis] consécutives.
 *
 * Le délai de grâce évite de recalculer à cause d'un simple saut du GPS.
 */
class OffRouteDetector(
    private val thresholdMeters: Double = 35.0,
    private val graceMillis: Long = 3_000,
) {
    private var offRouteSince: Long? = null

    /** Renvoie true au moment où il faut déclencher un recalcul. */
    fun update(distanceFromRouteMeters: Double, timeMillis: Long): Boolean {
        if (distanceFromRouteMeters <= thresholdMeters) {
            offRouteSince = null
            return false
        }
        val since = offRouteSince ?: timeMillis.also { offRouteSince = it }
        return timeMillis - since >= graceMillis
    }

    fun reset() {
        offRouteSince = null
    }
}
