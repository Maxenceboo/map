package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.data.routing.ManeuverType

/**
 * Convertit un code de manœuvre TomTom ("TURN_LEFT", "ROUNDABOUT_RIGHT"…) en [ManeuverType].
 * Référence : https://developer.tomtom.com/routing-api/documentation/tomtom-maps/calculate-route
 */
object TomTomManeuverMapper {

    fun map(code: String): ManeuverType = when (code.uppercase()) {
        "DEPART" -> ManeuverType.DEPART
        "ARRIVE", "ARRIVE_LEFT", "ARRIVE_RIGHT" -> ManeuverType.ARRIVE
        "BEAR_LEFT" -> ManeuverType.SLIGHT_LEFT
        "TURN_LEFT" -> ManeuverType.LEFT
        "SHARP_LEFT" -> ManeuverType.SHARP_LEFT
        "BEAR_RIGHT" -> ManeuverType.SLIGHT_RIGHT
        "TURN_RIGHT" -> ManeuverType.RIGHT
        "SHARP_RIGHT" -> ManeuverType.SHARP_RIGHT
        "KEEP_LEFT" -> ManeuverType.FORK_LEFT
        "KEEP_RIGHT" -> ManeuverType.FORK_RIGHT
        "MAKE_UTURN", "TRY_MAKE_UTURN" -> ManeuverType.UTURN
        "ROUNDABOUT_CROSS", "ROUNDABOUT_LEFT", "ROUNDABOUT_RIGHT", "ROUNDABOUT_BACK" -> ManeuverType.ROUNDABOUT
        "ENTER_MOTORWAY", "ENTER_FREEWAY", "ENTER_HIGHWAY" -> ManeuverType.MERGE
        "MOTORWAY_EXIT_LEFT" -> ManeuverType.RAMP_LEFT
        // En France, bretelles d'entrée et sorties sont à droite.
        "MOTORWAY_EXIT_RIGHT", "TAKE_EXIT", "ENTRANCE_RAMP" -> ManeuverType.RAMP_RIGHT
        // STRAIGHT, FOLLOW, SWITCH_PARALLEL_ROAD, SWITCH_MAIN_ROAD, TAKE_FERRY...
        else -> ManeuverType.STRAIGHT
    }
}
