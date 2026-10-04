package com.gamemaps.irl.car.mapping

import androidx.car.app.model.DateTimeWithZone
import androidx.car.app.navigation.model.TravelEstimate
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.navigation.RouteProgress
import java.util.TimeZone

/** Distance restante + heure d'arrivée, au format Android Auto. */
object CarTravelEstimateMapper {

    fun map(progress: RouteProgress, nowMillis: Long = System.currentTimeMillis()): TravelEstimate =
        estimate(progress.remainingDistanceMeters, progress.remainingDurationSeconds, nowMillis)

    /** Trajet complet, avant le départ. */
    fun forRoute(route: Route, nowMillis: Long = System.currentTimeMillis()): TravelEstimate =
        estimate(route.lengthMeters, route.durationSeconds, nowMillis)

    private fun estimate(distanceMeters: Double, durationSeconds: Double, nowMillis: Long): TravelEstimate {
        val arrivalMillis = nowMillis + (durationSeconds * 1_000).toLong()
        return TravelEstimate.Builder(
            CarDistanceMapper.map(distanceMeters),
            DateTimeWithZone.create(arrivalMillis, TimeZone.getDefault()),
        )
            .setRemainingTimeSeconds(durationSeconds.toLong())
            .build()
    }
}
