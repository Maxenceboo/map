package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.mirrored
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.taperedBody
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.wheels

/** Monoplace : nez long et fin, pontons, cockpit ouvert avec halo, ailerons avant et arrière, gros pneus (5,6 m × 2 m). */
object FormulaOne {

    val model = VehicleModel(
        headlightX = 0.25,
        parts = buildList {
            add(taperedBody(width = 0.9, noseWidth = 0.35, rearZ = -1.6, frontZ = 2.5, noseLength = 1.8, base = 0.15, top = 0.5, role = PartRole.BODY))
            // Pontons latéraux et capot moteur.
            addAll(mirrored { side -> box(side * 0.65, -0.4, 0.5, 1.6, 0.15, 0.5, PartRole.BODY) })
            add(box(0.0, -1.3, 0.5, 1.2, 0.5, 0.85, PartRole.BODY))
            // Cockpit et halo de sécurité.
            add(box(0.0, -0.2, 0.5, 0.9, 0.5, 0.62, PartRole.GLASS))
            add(box(0.0, 0.2, 0.5, 0.08, 0.62, 0.74, PartRole.TRIM))
            // Aileron avant large avec dérives.
            add(box(0.0, 2.6, 1.9, 0.35, 0.08, 0.16, PartRole.TRIM))
            addAll(mirrored { side -> box(side * 0.92, 2.6, 0.06, 0.4, 0.08, 0.3, PartRole.BODY) })
            // Aileron arrière haut, tenu par deux dérives.
            add(box(0.0, -2.6, 1.5, 0.35, 0.85, 0.95, PartRole.TRIM))
            addAll(mirrored { side -> box(side * 0.75, -2.6, 0.06, 0.5, 0.3, 0.95, PartRole.BODY) })
            addAll(wheels(trackWidth = 1.85, frontZ = 1.6, rearZ = -1.9, tireWidth = 0.42, diameter = 0.72))
            add(box(0.0, -2.82, 0.2, 0.06, 0.3, 0.45, PartRole.TAILLIGHT))
        },
    )
}
