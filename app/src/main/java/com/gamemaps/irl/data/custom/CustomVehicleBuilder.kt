package com.gamemaps.irl.data.custom

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehiclePart
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.mirrored
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.taperedBody
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.wheels

/** Assemble le modèle 3D d'un véhicule personnalisé : caisse, habitacle, roues, feux, aileron. Calcul pur. */
object CustomVehicleBuilder {

    fun build(raw: CustomVehicleSpec): VehicleModel {
        val spec = raw.clamped()
        val half = spec.length / 2
        val bodyBase = spec.groundClearance
        val bodyTop = bodyBase + spec.bodyHeight
        val cabinLength = spec.length * spec.cabinRatio
        val cabinCenterZ = -spec.length * 0.08 // l'habitacle est un peu en arrière, comme sur une vraie voiture
        val cabinTop = bodyTop + spec.cabinHeight
        val lightBase = bodyBase + spec.bodyHeight * 0.45
        val lightTop = bodyBase + spec.bodyHeight * 0.85
        val headlightX = spec.width * 0.3

        val parts = buildList<VehiclePart> {
            add(taperedBody(spec.width, spec.width * 0.75, -half, half, spec.length * 0.15, bodyBase, bodyTop, PartRole.BODY))
            add(box(0.0, cabinCenterZ, spec.width * 0.82, cabinLength, bodyTop, cabinTop, PartRole.GLASS))
            add(box(0.0, cabinCenterZ, spec.width * 0.76, cabinLength * 0.65, cabinTop, cabinTop + ROOF_THICKNESS, PartRole.BODY))
            addAll(wheels(spec.width * 0.92, half * 0.62, -half * 0.62, TIRE_WIDTH, spec.wheelDiameter))
            addAll(mirrored { side -> box(side * headlightX, half - 0.06, spec.width * 0.2, 0.1, lightBase, lightTop, PartRole.HEADLIGHT) })
            addAll(mirrored { side -> box(side * spec.width * 0.33, -half + 0.04, spec.width * 0.22, 0.08, lightBase, lightTop, PartRole.TAILLIGHT) })
            if (spec.spoiler) {
                val wingZ = -half + 0.2
                add(box(0.0, wingZ, spec.width * 0.95, 0.3, bodyTop + 0.28, bodyTop + 0.36, PartRole.TRIM))
                addAll(mirrored { side -> box(side * spec.width * 0.35, wingZ, 0.08, 0.2, bodyTop, bodyTop + 0.28, PartRole.TRIM) })
            }
        }
        return VehicleModel(parts = parts, headlightX = headlightX)
    }

    private const val ROOF_THICKNESS = 0.05
    private const val TIRE_WIDTH = 0.28
}
