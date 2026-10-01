package com.gamemaps.irl.ui.hud

import com.gamemaps.irl.core.format.ArrivalTimeFormatter
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.format.DurationFormatter
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.instructions.InstructionTextBuilder
import com.gamemaps.irl.navigation.instructions.ManeuverGlyphs

/** Textes prêts à afficher pour le HUD de guidage. Les composables restent ainsi "bêtes". */
data class HudModel(
    val maneuverGlyph: String,
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
        maneuverGlyph = step?.let { ManeuverGlyphs.glyph(it.maneuver) } ?: "",
        distanceToManeuver = DistanceFormatter.format(progress.distanceToNextStepMeters),
        instruction = step?.let(InstructionTextBuilder::build) ?: destination.name,
        arrivalTime = ArrivalTimeFormatter.format(nowMillis, progress.remainingDurationSeconds),
        remainingDistance = DistanceFormatter.format(progress.remainingDistanceMeters),
        remainingDuration = DurationFormatter.format(progress.remainingDurationSeconds),
        isRerouting = isRerouting,
    )
}
