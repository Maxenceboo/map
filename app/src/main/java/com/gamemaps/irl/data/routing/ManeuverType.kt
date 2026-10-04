package com.gamemaps.irl.data.routing

/**
 * Type de manœuvre, indépendant du moteur de routage.
 * OSRM et TomTom sont convertis vers cette liste.
 */
enum class ManeuverType {
    DEPART,
    ARRIVE,
    STRAIGHT,
    SLIGHT_LEFT,
    LEFT,
    SHARP_LEFT,
    SLIGHT_RIGHT,
    RIGHT,
    SHARP_RIGHT,
    UTURN,
    ROUNDABOUT,
    MERGE,
    RAMP_LEFT,
    RAMP_RIGHT,
    FORK_LEFT,
    FORK_RIGHT,
}
