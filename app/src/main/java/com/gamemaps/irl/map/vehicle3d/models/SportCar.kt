package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.mirrored
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.taperedBody
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.wheels

/** Coupé sport : châssis surbaissé au nez effilé, habitacle teinté, aileron arrière (4,4 m × 1,9 m). */
object SportCar {

    val model = VehicleModel(
        headlightX = 0.5,
        parts = buildList {
            add(taperedBody(width = 1.9, noseWidth = 1.3, rearZ = -2.2, frontZ = 2.2, noseLength = 0.8, base = 0.25, top = 0.72, role = PartRole.BODY))
            add(box(0.0, -0.35, 1.55, 1.9, 0.72, 1.15, PartRole.GLASS))
            add(box(0.0, -0.4, 1.45, 1.2, 1.15, 1.2, PartRole.BODY))
            // Aileron et ses deux supports.
            add(box(0.0, -2.05, 1.8, 0.3, 1.0, 1.08, PartRole.TRIM))
            addAll(mirrored { side -> box(side * 0.7, -2.05, 0.08, 0.2, 0.72, 1.0, PartRole.TRIM) })
            addAll(wheels(trackWidth = 1.75, frontZ = 1.35, rearZ = -1.35, tireWidth = 0.28, diameter = 0.66))
            addAll(mirrored { side -> box(side * 0.5, 2.14, 0.35, 0.1, 0.5, 0.68, PartRole.HEADLIGHT) })
            addAll(mirrored { side -> box(side * 0.7, -2.18, 0.4, 0.08, 0.5, 0.68, PartRole.TAILLIGHT) })
        },
    )
}
