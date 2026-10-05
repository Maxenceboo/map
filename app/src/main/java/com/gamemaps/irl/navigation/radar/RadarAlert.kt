package com.gamemaps.irl.navigation.radar

/**
 * Le véhicule est dans une zone de danger.
 *
 * Volontairement, l'alerte ne dit ni où se trouve le contrôle ni à quelle distance :
 * en France, signaler l'emplacement précis d'un radar est interdit, seules les "zones de danger" sont permises.
 *
 * @property zoneId identifiant de la zone, pour ne l'annoncer qu'une fois.
 * @property maxSpeedKmh vitesse autorisée dans la zone, si connue.
 */
data class RadarAlert(
    val zoneId: String,
    val maxSpeedKmh: Int?,
)
