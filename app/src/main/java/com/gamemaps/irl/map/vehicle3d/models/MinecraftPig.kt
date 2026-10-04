package com.gamemaps.irl.map.vehicle3d.models

import com.gamemaps.irl.map.vehicle3d.PartRole
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.box
import com.gamemaps.irl.map.vehicle3d.VehicleShapes.mirrored

/**
 * Cochon du thème Minecraft, tout en cubes : corps, tête, groin, quatre pattes et deux yeux.
 * Ses couleurs sont fixes (il ne prend pas la couleur de carrosserie).
 */
object MinecraftPig {

    private const val PINK = "#f0a5a2"
    private const val DARK_PINK = "#d4807e"
    private const val EYE = "#1a1a1a"

    val model = VehicleModel(
        headlightX = 0.0,
        parts = listOf(
            // Corps et tête.
            box(0.0, -0.3, 1.3, 2.2, 0.55, 1.45, PartRole.BODY, PINK),
            box(0.0, 1.25, 1.1, 1.0, 0.75, 1.8, PartRole.BODY, PINK),
            // Groin.
            box(0.0, 1.82, 0.55, 0.14, 0.9, 1.25, PartRole.TRIM, DARK_PINK),
        ) +
            // Yeux, de chaque côté du groin.
            mirrored { side -> box(side * 0.38, 1.77, 0.22, 0.04, 1.35, 1.55, PartRole.TRIM, EYE) } +
            // Pattes avant et arrière.
            mirrored { side -> box(side * 0.42, 0.45, 0.42, 0.42, 0.0, 0.55, PartRole.BODY, DARK_PINK) } +
            mirrored { side -> box(side * 0.42, -1.05, 0.42, 0.42, 0.0, 0.55, PartRole.BODY, DARK_PINK) },
    )
}
