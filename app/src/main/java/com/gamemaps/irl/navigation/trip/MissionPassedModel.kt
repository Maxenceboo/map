package com.gamemaps.irl.navigation.trip

import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.format.DurationFormatter
import com.gamemaps.irl.navigation.NavigationState
import kotlin.math.roundToInt

/** Bilan du trajet mis en forme, pour l'écran "Mission accomplie" (téléphone) et le message d'arrivée (voiture). */
data class MissionPassedModel(
    val destinationName: String,
    val distance: String,
    val duration: String,
    val averageSpeed: String,
)

fun NavigationState.Arrived.toMissionPassedModel() = MissionPassedModel(
    destinationName = destination.name,
    distance = DistanceFormatter.format(stats.distanceMeters),
    duration = DurationFormatter.format(stats.durationSeconds),
    averageSpeed = "${stats.averageSpeedKmh.roundToInt()} km/h",
)
