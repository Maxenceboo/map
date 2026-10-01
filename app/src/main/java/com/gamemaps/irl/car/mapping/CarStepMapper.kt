package com.gamemaps.irl.car.mapping

import androidx.car.app.navigation.model.Maneuver
import androidx.car.app.navigation.model.Step
import com.gamemaps.irl.data.routing.RouteStep
import com.gamemaps.irl.navigation.instructions.InstructionTextBuilder

/** [RouteStep] → [Step] Android Auto : manœuvre + icône + phrase + nom de voie. */
object CarStepMapper {

    fun map(step: RouteStep): Step {
        val maneuverBuilder = Maneuver.Builder(CarManeuverMapper.type(step.maneuver, step.roundaboutExit))
            .setIcon(ManeuverIconFactory.icon(step.maneuver))
        val exit = step.roundaboutExit
        if (exit != null && exit >= 1) maneuverBuilder.setRoundaboutExitNumber(exit)

        val builder = Step.Builder(InstructionTextBuilder.build(step)).setManeuver(maneuverBuilder.build())
        if (step.roadName.isNotBlank()) builder.setRoad(step.roadName)
        return builder.build()
    }
}
