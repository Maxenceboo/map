package com.gamemaps.irl.ui.hud

import com.gamemaps.irl.core.format.ArrivalTimeFormatter
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.format.DurationFormatter
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.instructions.InstructionTextBuilder

/**
 * Contenu du HUD de guidage, prêt à afficher. Les composables restent ainsi "bêtes".
 *
 * @property maneuver prochaine manœuvre (dessinée par [ManeuverIcon]) ; null s'il n'y en a plus.
 * @property roundaboutExit numéro de sortie, affiché au centre du rond-point.
 */
data class HudModel(
    val maneuver: ManeuverType?,
    val roundaboutExit: Int?,
    val distanceToManeuver: String,
    val instruction: String,
    val arrivalTime: String,
    val remainingDistance: String,
    val remainingDuration: String,
    val isRerouting: Boolean,
)

fun NavigationState.Navigating.toHudModel(nowMillis: Long = System.currentTimeMillis()): HudModel {
    val step = progress.nextStep
    return HudModel(
        maneuver = step?.maneuver,
        roundaboutExit = step?.roundaboutExit,
        distanceToManeuver = DistanceFormatter.format(progress.distanceToNextStepMeters),
        instruction = step?.let(InstructionTextBuilder::build) ?: destination.name,
        arrivalTime = ArrivalTimeFormatter.format(nowMillis, progress.remainingDurationSeconds),
        remainingDistance = DistanceFormatter.format(progress.remainingDistanceMeters),
        remainingDuration = DurationFormatter.format(progress.remainingDurationSeconds),
        isRerouting = isRerouting,
    )
}
