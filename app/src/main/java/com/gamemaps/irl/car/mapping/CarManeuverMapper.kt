package com.gamemaps.irl.car.mapping

import androidx.car.app.navigation.model.Maneuver
import com.gamemaps.irl.data.routing.ManeuverType

/**
 * Traduit notre [ManeuverType] en type de manœuvre Android Auto.
 * Ronds-points dans le sens antihoraire (CCW) : on roule à droite en France.
 */
object CarManeuverMapper {

    fun type(maneuver: ManeuverType, roundaboutExit: Int?): Int = when (maneuver) {
        ManeuverType.DEPART -> Maneuver.TYPE_DEPART
        ManeuverType.ARRIVE -> Maneuver.TYPE_DESTINATION
        ManeuverType.STRAIGHT -> Maneuver.TYPE_STRAIGHT
        ManeuverType.SLIGHT_LEFT -> Maneuver.TYPE_TURN_SLIGHT_LEFT
        ManeuverType.LEFT -> Maneuver.TYPE_TURN_NORMAL_LEFT
        ManeuverType.SHARP_LEFT -> Maneuver.TYPE_TURN_SHARP_LEFT
        ManeuverType.SLIGHT_RIGHT -> Maneuver.TYPE_TURN_SLIGHT_RIGHT
        ManeuverType.RIGHT -> Maneuver.TYPE_TURN_NORMAL_RIGHT
        ManeuverType.SHARP_RIGHT -> Maneuver.TYPE_TURN_SHARP_RIGHT
        ManeuverType.UTURN -> Maneuver.TYPE_U_TURN_LEFT
        ManeuverType.MERGE -> Maneuver.TYPE_MERGE_SIDE_UNSPECIFIED
        ManeuverType.RAMP_LEFT -> Maneuver.TYPE_OFF_RAMP_SLIGHT_LEFT
        ManeuverType.RAMP_RIGHT -> Maneuver.TYPE_OFF_RAMP_SLIGHT_RIGHT
        ManeuverType.FORK_LEFT -> Maneuver.TYPE_FORK_LEFT
        ManeuverType.FORK_RIGHT -> Maneuver.TYPE_FORK_RIGHT
        ManeuverType.ROUNDABOUT ->
            if (roundaboutExit != null && roundaboutExit >= 1) {
                Maneuver.TYPE_ROUNDABOUT_ENTER_AND_EXIT_CCW
            } else {
                Maneuver.TYPE_ROUNDABOUT_ENTER_CCW
            }
    }
}
