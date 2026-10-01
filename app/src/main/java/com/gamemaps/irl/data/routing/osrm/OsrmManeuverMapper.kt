package com.gamemaps.irl.data.routing.osrm

import com.gamemaps.irl.data.routing.ManeuverType

/**
 * Convertit le couple OSRM (`maneuver.type`, `maneuver.modifier`) en [ManeuverType].
 * Référence : https://project-osrm.org/docs/v5.24.0/api/#stepmaneuver-object
 */
object OsrmManeuverMapper {

    fun map(type: String, modifier: String?): ManeuverType = when (type) {
        "depart" -> ManeuverType.DEPART
        "arrive" -> ManeuverType.ARRIVE
        "roundabout", "rotary" -> ManeuverType.ROUNDABOUT
        "merge" -> ManeuverType.MERGE
        "on ramp", "off ramp" -> if (isLeft(modifier)) ManeuverType.RAMP_LEFT else ManeuverType.RAMP_RIGHT
        "fork" -> if (isLeft(modifier)) ManeuverType.FORK_LEFT else ManeuverType.FORK_RIGHT
        // turn, new name, continue, end of road, roundabout turn, notification...
        else -> fromModifier(modifier)
    }

    private fun fromModifier(modifier: String?): ManeuverType = when (modifier) {
        "uturn" -> ManeuverType.UTURN
        "sharp left" -> ManeuverType.SHARP_LEFT
        "left" -> ManeuverType.LEFT
        "slight left" -> ManeuverType.SLIGHT_LEFT
        "sharp right" -> ManeuverType.SHARP_RIGHT
        "right" -> ManeuverType.RIGHT
        "slight right" -> ManeuverType.SLIGHT_RIGHT
        else -> ManeuverType.STRAIGHT
    }

    private fun isLeft(modifier: String?): Boolean = modifier?.contains("left") == true
}
