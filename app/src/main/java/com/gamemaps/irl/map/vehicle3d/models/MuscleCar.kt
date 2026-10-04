package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.mirrored
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.taperedBody
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.wheels

/** Muscle car américaine : lignes carrées, long capot avec prise d'air, échappements latéraux (4,9 m × 1,95 m). */
object MuscleCar {

    val model = VehicleModel(
        headlightX = 0.65,
        parts = buildList {
            add(taperedBody(width = 1.95, noseWidth = 1.8, rearZ = -2.45, frontZ = 2.45, noseLength = 0.3, base = 0.3, top = 0.85, role = PartRole.BODY))
            // Prise d'air ("blower") qui dépasse du capot.
            add(box(0.0, 1.3, 0.5, 0.7, 0.85, 1.05, PartRole.TRIM))
            add(box(0.0, -0.5, 1.6, 1.8, 0.85, 1.3, PartRole.GLASS))
            add(box(0.0, -0.5, 1.5, 1.3, 1.3, 1.35, PartRole.BODY))
            // Échappements latéraux.
            addAll(mirrored { side -> box(side * 1.0, -0.3, 0.1, 1.6, 0.2, 0.32, PartRole.TRIM) })
            addAll(wheels(trackWidth = 1.8, frontZ = 1.5, rearZ = -1.5, tireWidth = 0.3, diameter = 0.72))
            addAll(mirrored { side -> box(side * 0.65, 2.4, 0.4, 0.1, 0.5, 0.72, PartRole.HEADLIGHT) })
            addAll(mirrored { side -> box(side * 0.7, -2.42, 0.45, 0.08, 0.5, 0.72, PartRole.TAILLIGHT) })
        },
    )
}
