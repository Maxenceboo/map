package com.gamemaps.irl.map.vehicle3d

/**
 * Un véhicule 3D : une liste de pièces, en dimensions réelles (mètres).
 * Il est agrandi à l'affichage pour rester bien visible à l'écran (voir [VehicleScale]).
 *
 * @property headlightX écart latéral des phares par rapport à l'axe (0 = un seul phare central, pour une moto).
 */
data class VehicleModel(
    val parts: List<VehiclePart>,
    val headlightX: Double,
) {
    /** Longueur hors tout, de l'arrière à l'avant. */
    val lengthMeters: Double = parts.flatMap { it.footprint }.let { points -> points.maxOf { it.z } - points.minOf { it.z } }

    /** Position de l'avant du véhicule (d'où partent les faisceaux des phares). */
    val frontZ: Double = parts.flatMap { it.footprint }.maxOf { it.z }
}
