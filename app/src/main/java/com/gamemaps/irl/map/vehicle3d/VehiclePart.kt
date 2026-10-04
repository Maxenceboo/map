package com.gamemaps.irl.map.vehicle3d

/** Un point au sol dans le repère du véhicule, en mètres : [x] vers la droite, [z] vers l'avant. */
data class GroundPoint(val x: Double, val z: Double)

/**
 * Une pièce du véhicule : un contour au sol ([footprint]) "tiré" verticalement de [baseMeters] à [topMeters].
 *
 * [colorHex] impose une couleur à la pièce ; null = la couleur de son [role] (voir [VehiclePalette]).
 *
 * Même convention que la version WebGL (cahier des charges §3.2) : +Z = avant, +X = flanc droit, Y = hauteur.
 */
data class VehiclePart(
    val footprint: List<GroundPoint>,
    val baseMeters: Double,
    val topMeters: Double,
    val role: PartRole,
    val colorHex: String? = null,
)
