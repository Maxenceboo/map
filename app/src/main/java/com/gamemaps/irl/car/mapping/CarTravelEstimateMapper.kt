package com.gamemaps.irl.car.mapping

import androidx.car.app.model.DateTimeWithZone
import androidx.car.app.navigation.model.TravelEstimate
import com.gamemaps.irl.navigation.RouteProgress
import java.util.TimeZone

/** Distance restante + heure d'arrivée, au format Android Auto. */
object CarTravelEstimateMapper {

    fun map(progress: RouteProgress, nowMillis: Long = System.currentTimeMillis()): TravelEstimate {
        val arrivalMillis = nowMillis + (progress.remainingDurationSeconds * 1_000).toLong()
        return TravelEstimate.Builder(
            CarDistanceMapper.map(progress.remainingDistanceMeters),
            DateTimeWithZone.create(arrivalMillis, TimeZone.getDefault()),
        )
            .setRemainingTimeSeconds(progress.remainingDurationSeconds.toLong())
            .build()
    }
}
