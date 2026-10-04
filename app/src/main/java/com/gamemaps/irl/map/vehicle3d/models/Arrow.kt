package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.GroundPoint
import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehiclePart

/**
 * Curseur de la minimap GTA en relief : une pointe de flèche épaisse, posée sur un socle sombre
 * un peu plus large qui lui fait un contour (4,6 m × 3 m).
 */
object Arrow {

    /** Pointe de flèche : le nez, l'aile droite, l'encoche arrière, l'aile gauche. */
    private fun chevron(tipZ: Double, halfWidth: Double, rearZ: Double, notchZ: Double) = listOf(
        GroundPoint(0.0, tipZ),
        GroundPoint(halfWidth, rearZ),
        GroundPoint(0.0, notchZ),
        GroundPoint(-halfWidth, rearZ),
    )

    val model = VehicleModel(
        headlightX = 0.0,
        parts = listOf(
            VehiclePart(chevron(tipZ = 2.3, halfWidth = 1.5, rearZ = -2.3, notchZ = -1.15), baseMeters = 0.0, topMeters = 0.25, role = PartRole.TRIM),
            VehiclePart(chevron(tipZ = 1.95, halfWidth = 1.2, rearZ = -1.9, notchZ = -0.95), baseMeters = 0.25, topMeters = 0.9, role = PartRole.BODY),
        ),
    )
}
