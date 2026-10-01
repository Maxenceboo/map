package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng

/**
 * Une étape de l'itinéraire : la manœuvre à effectuer, puis le tronçon qui suit.
 *
 * @property location point où la manœuvre a lieu.
 * @property roadName nom de la voie empruntée après la manœuvre (peut être vide).
 * @property roundaboutExit numéro de sortie si [maneuver] est un rond-point.
 * @property startDistanceMeters position de la manœuvre le long du tracé, depuis le départ.
 * @property distanceMeters longueur du tronçon jusqu'à la manœuvre suivante.
 */
data class RouteStep(
    val maneuver: ManeuverType,
    val location: LatLng,
    val roadName: String,
    val roundaboutExit: Int?,
    val startDistanceMeters: Double,
    val distanceMeters: Double,
    val durationSeconds: Double,
)
