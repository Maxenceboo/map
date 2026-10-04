package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.instructions.InstructionTextBuilder

/**
 * Décide des annonces de guidage à partir de l'état de navigation.
 *
 * - Démarrage : carillon + première instruction.
 * - Chaque manœuvre est annoncée deux fois : de loin ("Dans 500 mètres, tournez à droite…")
 *   puis juste avant (carillon + "Tournez à droite…").
 * - Recalcul, arrivée, échec : une phrase dédiée.
 *
 * Logique pure : reçoit des états, renvoie des [AudioCue]. Testable sans téléphone.
 */
class GuidanceAnnouncer(
    private val farMeters: Double = 500.0,
    private val nearMeters: Double = 80.0,
) {
    private var lastState: NavigationState = NavigationState.Idle
    private var lastRoute: Route? = null
    private val announcedFar = mutableSetOf<Int>()
    private val announcedNear = mutableSetOf<Int>()

    fun onState(state: NavigationState): List<AudioCue> {
        val cues = when (state) {
            is NavigationState.Navigating -> onNavigating(state)
            is NavigationState.Arrived ->
                if (lastState !is NavigationState.Arrived) {
                    listOf(AudioCue.Sound(SoundEffect.MISSION_PASSED), AudioCue.Speech("Mission accomplie. Vous êtes arrivé à destination"))
                } else emptyList()
            is NavigationState.Failed ->
                if (lastState !is NavigationState.Failed) listOf(AudioCue.Speech("Itinéraire introuvable")) else emptyList()
            else -> emptyList()
        }
        lastState = state
        return cues
    }

    private fun onNavigating(state: NavigationState.Navigating): List<AudioCue> {
        val cues = mutableListOf<AudioCue>()
        val previous = lastState as? NavigationState.Navigating

        if (previous == null) {
            resetFor(state.route)
            cues += AudioCue.Sound(SoundEffect.START)
            state.route.steps.firstOrNull()?.let { cues += AudioCue.Speech(InstructionTextBuilder.build(it)) }
        } else if (state.route !== lastRoute) {
            resetFor(state.route) // Nouvel itinéraire après un recalcul : les annonces repartent de zéro.
        }
        if (state.isRerouting && previous?.isRerouting != true) {
            cues += AudioCue.Speech("Recalcul de l'itinéraire")
        }
        cues += maneuverCues(state)
        return cues
    }

    private fun maneuverCues(state: NavigationState.Navigating): List<AudioCue> {
        val step = state.progress.nextStep ?: return emptyList()
        if (step.maneuver == ManeuverType.ARRIVE || state.isRerouting) return emptyList()
        val index = state.route.steps.indexOf(step)
        val distance = state.progress.distanceToNextStepMeters
        val instruction = InstructionTextBuilder.build(step)

        return when {
            distance <= nearMeters && announcedNear.add(index) -> {
                announcedFar += index
                listOf(AudioCue.Sound(SoundEffect.TURN), AudioCue.Speech(instruction))
            }
            // Pas d'annonce "de loin" si la manœuvre est déjà presque là : l'annonce proche suffira.
            distance <= farMeters && distance > nearMeters * 2 && announcedFar.add(index) ->
                listOf(AudioCue.Speech("Dans ${SpeechDistanceFormatter.format(distance)}, ${instruction.lowercaseFirst()}"))
            else -> emptyList()
        }
    }

    private fun resetFor(route: Route) {
        lastRoute = route
        announcedFar.clear()
        announcedNear.clear()
    }

    private fun String.lowercaseFirst(): String = replaceFirstChar { it.lowercase() }
}
