package com.gamemaps.irl.navigation

/** Considère la destination atteinte quand il reste moins de [radiusMeters] sur le tracé. */
class ArrivalDetector(private val radiusMeters: Double = 30.0) {
    fun hasArrived(progress: RouteProgress): Boolean = progress.remainingDistanceMeters < radiusMeters
}
