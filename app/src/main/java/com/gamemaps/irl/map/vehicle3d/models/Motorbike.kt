package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.taperedBody

/** Moto hypersport avec son pilote : deux roues en ligne, carénage plongeant, bulle, guidon (2,2 m × 0,7 m). */
object Motorbike {

    val model = VehicleModel(
        headlightX = 0.0,
        parts = listOf(
            // Roues avant et arrière, en ligne.
            box(0.0, 0.78, 0.16, 0.62, 0.0, 0.62, PartRole.TIRE),
            box(0.0, -0.78, 0.2, 0.64, 0.0, 0.64, PartRole.TIRE),
            // Cadre, carénage avant et bulle.
            box(0.0, 0.0, 0.3, 1.2, 0.4, 0.8, PartRole.BODY),
            taperedBody(width = 0.5, noseWidth = 0.2, rearZ = 0.2, frontZ = 1.05, noseLength = 0.5, base = 0.6, top = 1.05, role = PartRole.BODY),
            box(0.0, 0.75, 0.3, 0.15, 1.05, 1.2, PartRole.GLASS),
            box(0.0, 0.55, 0.7, 0.06, 1.0, 1.06, PartRole.TRIM),
            // Selle, pilote et casque (aux couleurs de la moto).
            box(0.0, -0.5, 0.28, 0.6, 0.8, 0.9, PartRole.TRIM),
            box(0.0, -0.15, 0.45, 0.4, 0.9, 1.45, PartRole.TRIM),
            box(0.0, 0.05, 0.28, 0.3, 1.45, 1.72, PartRole.BODY),
            box(0.0, 1.08, 0.18, 0.06, 0.8, 0.95, PartRole.HEADLIGHT),
            box(0.0, -1.08, 0.16, 0.05, 0.75, 0.85, PartRole.TAILLIGHT),
        ),
    )
}
