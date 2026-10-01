package com.gamemaps.irl.navigation.instructions

import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.RouteStep

/** Phrase de guidage en français pour une étape : "Tournez à gauche sur Rue Sainte-Catherine". */
object InstructionTextBuilder {

    fun build(step: RouteStep): String {
        val road = step.roadName.takeIf { it.isNotBlank() }
        return when (step.maneuver) {
            ManeuverType.DEPART -> road?.let { "Partez sur $it" } ?: "Partez"
            ManeuverType.ARRIVE -> "Vous êtes arrivé à destination"
            ManeuverType.ROUNDABOUT -> roundabout(step.roundaboutExit, road)
            ManeuverType.RAMP_LEFT, ManeuverType.RAMP_RIGHT,
            ManeuverType.FORK_LEFT, ManeuverType.FORK_RIGHT,
            ManeuverType.MERGE -> withRoad(verb(step.maneuver), road, "vers")
            else -> withRoad(verb(step.maneuver), road, "sur")
        }
    }

    private fun verb(type: ManeuverType): String = when (type) {
        ManeuverType.STRAIGHT -> "Continuez tout droit"
        ManeuverType.SLIGHT_LEFT -> "Serrez à gauche"
        ManeuverType.LEFT -> "Tournez à gauche"
        ManeuverType.SHARP_LEFT -> "Tournez franchement à gauche"
        ManeuverType.SLIGHT_RIGHT -> "Serrez à droite"
        ManeuverType.RIGHT -> "Tournez à droite"
        ManeuverType.SHARP_RIGHT -> "Tournez franchement à droite"
        ManeuverType.UTURN -> "Faites demi-tour"
        ManeuverType.MERGE -> "Insérez-vous"
        ManeuverType.RAMP_LEFT -> "Prenez la bretelle à gauche"
        ManeuverType.RAMP_RIGHT -> "Prenez la bretelle à droite"
        ManeuverType.FORK_LEFT -> "Restez à gauche"
        ManeuverType.FORK_RIGHT -> "Restez à droite"
        ManeuverType.DEPART, ManeuverType.ARRIVE, ManeuverType.ROUNDABOUT -> ""
    }

    private fun roundabout(exit: Int?, road: String?): String {
        val base = if (exit != null) "Au rond-point, prenez la ${ordinal(exit)} sortie" else "Au rond-point, continuez"
        return withRoad(base, road, "vers")
    }

    private fun withRoad(base: String, road: String?, preposition: String): String =
        if (road == null) base else "$base $preposition $road"

    /** 1 → "1re", 2 → "2e"... */
    fun ordinal(n: Int): String = if (n == 1) "1re" else "${n}e"
}
