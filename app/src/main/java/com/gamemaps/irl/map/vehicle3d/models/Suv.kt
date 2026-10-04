package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.mirrored
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.wheels

/** 4x4 / baroudeur : garde au sol rehaussée, habitacle haut, pare-chocs renforcés, barres de toit (4,9 m × 2,05 m). */
object Suv {

    val model = VehicleModel(
        headlightX = 0.7,
        parts = buildList {
            add(box(0.0, 0.0, 2.0, 4.7, 0.45, 1.05, PartRole.BODY))
            add(box(0.0, -0.3, 1.85, 3.0, 1.05, 1.75, PartRole.GLASS))
            add(box(0.0, -0.3, 1.8, 2.9, 1.75, 1.8, PartRole.BODY))
            // Barres de toit longitudinales.
            addAll(mirrored { side -> box(side * 0.75, -0.3, 0.08, 2.6, 1.8, 1.9, PartRole.TRIM) })
            // Pare-chocs avant et arrière.
            add(box(0.0, 2.4, 2.05, 0.2, 0.35, 0.7, PartRole.TRIM))
            add(box(0.0, -2.4, 2.05, 0.2, 0.35, 0.7, PartRole.TRIM))
            addAll(wheels(trackWidth = 1.9, frontZ = 1.5, rearZ = -1.5, tireWidth = 0.32, diameter = 0.85))
            addAll(mirrored { side -> box(side * 0.7, 2.32, 0.4, 0.1, 0.75, 0.98, PartRole.HEADLIGHT) })
            addAll(mirrored { side -> box(side * 0.75, -2.32, 0.35, 0.08, 0.75, 1.0, PartRole.TAILLIGHT) })
        },
    )
}
