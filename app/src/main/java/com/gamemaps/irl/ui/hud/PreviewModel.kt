package com.gamemaps.irl.ui.hud

import com.gamemaps.irl.core.format.ArrivalTimeFormatter
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.format.DurationFormatter
import com.gamemaps.irl.navigation.NavigationState

/** Textes de l'aperçu d'itinéraire, prêts à afficher. */
data class PreviewModel(
    val destinationName: String,
    val destinationSubtitle: String,
    val distance: String,
    val duration: String,
    val arrivalTime: String,
)

fun NavigationState.Previewing.toPreviewModel(nowMillis: Long = System.currentTimeMillis()) = PreviewModel(
    destinationName = destination.name,
    destinationSubtitle = destination.subtitle,
    distance = DistanceFormatter.format(route.lengthMeters),
    duration = DurationFormatter.format(route.durationSeconds),
    arrivalTime = ArrivalTimeFormatter.format(nowMillis, route.durationSeconds),
)
