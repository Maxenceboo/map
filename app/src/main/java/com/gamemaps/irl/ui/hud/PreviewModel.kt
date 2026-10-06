package com.gamemaps.irl.ui.hud

import com.gamemaps.irl.core.format.ArrivalTimeFormatter
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.format.DurationFormatter
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.navigation.NavigationState

/** Textes de l'aperçu d'itinéraire, prêts à afficher. */
data class PreviewModel(
    val destinationName: String,
    val destinationSubtitle: String,
    val distance: String,
    val duration: String,
    val arrivalTime: String,
    /** "+ 6 min de bouchons", ou null si le trafic est fluide ou inconnu. */
    val trafficDelay: String?,
    /** Trajets proposés ; un seul = pas de choix à afficher. */
    val options: List<RouteOption> = emptyList(),
)

/** Un trajet proposé dans l'aperçu. */
data class RouteOption(val label: String, val duration: String, val distance: String, val selected: Boolean)

fun NavigationState.Previewing.toPreviewModel(nowMillis: Long = System.currentTimeMillis()) = PreviewModel(
    destinationName = destination.name,
    destinationSubtitle = destination.subtitle,
    distance = DistanceFormatter.format(route.lengthMeters),
    duration = DurationFormatter.format(route.durationSeconds),
    arrivalTime = ArrivalTimeFormatter.format(nowMillis, route.durationSeconds),
    trafficDelay = route.trafficDelaySeconds.takeIf { it >= 60 }?.let { "+ ${DurationFormatter.format(it)} de bouchons" },
    options = alternatives.mapIndexed { index, option ->
        RouteOption(
            label = routeLabel(index, option, alternatives),
            duration = DurationFormatter.format(option.durationSeconds),
            distance = DistanceFormatter.format(option.lengthMeters),
            selected = option === route,
        )
    },
)

/** Le premier trajet est celui que conseille le moteur ; parmi les autres, on signale le plus court. */
private fun routeLabel(index: Int, option: Route, all: List<Route>): String = when {
    index == 0 -> "Conseillé"
    option.lengthMeters == all.minOf { it.lengthMeters } -> "Plus court"
    else -> "Autre trajet"
}
