package com.gamemaps.irl.service.notification

import com.gamemaps.irl.core.format.ArrivalTimeFormatter
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.format.DurationFormatter
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.instructions.InstructionTextBuilder

/**
 * Textes de la notification de guidage (logique pure, testable) :
 * - titre : "180 m · Tournez à droite sur Rue des Arbousiers"
 * - texte : "Arrivée 17:47 · 57 km · 1 h 05"
 */
data class NavigationNotificationContent(val title: String, val text: String) {

    companion object {
        fun from(state: NavigationState, nowMillis: Long = System.currentTimeMillis()): NavigationNotificationContent = when (state) {
            is NavigationState.Navigating -> navigating(state, nowMillis)
            is NavigationState.Calculating -> NavigationNotificationContent("Calcul de l'itinéraire…", state.destination.name)
            is NavigationState.Previewing -> NavigationNotificationContent("Itinéraire prêt", state.destination.name)
            is NavigationState.Arrived -> NavigationNotificationContent("Vous êtes arrivé", state.destination.name)
            is NavigationState.Failed -> NavigationNotificationContent("Itinéraire introuvable", state.destination.name)
            NavigationState.Idle -> NavigationNotificationContent("Game Maps IRL", "Guidage arrêté")
        }

        private fun navigating(state: NavigationState.Navigating, nowMillis: Long): NavigationNotificationContent {
            val progress = state.progress
            val step = progress.nextStep
            val title = when {
                state.isRerouting -> "Recalcul de l'itinéraire…"
                step == null -> state.destination.name
                else -> "${DistanceFormatter.format(progress.distanceToNextStepMeters)} · ${InstructionTextBuilder.build(step)}"
            }
            val text = listOf(
                "Arrivée ${ArrivalTimeFormatter.format(nowMillis, progress.remainingDurationSeconds)}",
                DistanceFormatter.format(progress.remainingDistanceMeters),
                DurationFormatter.format(progress.remainingDurationSeconds),
            ).joinToString(" · ")
            return NavigationNotificationContent(title, text)
        }
    }
}
