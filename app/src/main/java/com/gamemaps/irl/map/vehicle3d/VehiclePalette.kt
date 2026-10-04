package com.gamemaps.irl.map.vehicle3d

/** Couleur de chaque pièce : la carrosserie prend la couleur choisie, le reste est fixe. */
object VehiclePalette {

    private const val GLASS = "#0f172a"
    private const val TIRE = "#0a0a0a"
    private const val TRIM = "#1f2937"
    private const val HEADLIGHT = "#fef9c3"
    private const val TAILLIGHT = "#dc2626"

    fun hexFor(role: PartRole, bodyColor: VehicleColor): String = when (role) {
        PartRole.BODY -> bodyColor.hex
        PartRole.GLASS -> GLASS
        PartRole.TIRE -> TIRE
        PartRole.TRIM -> TRIM
        PartRole.HEADLIGHT -> HEADLIGHT
        PartRole.TAILLIGHT -> TAILLIGHT
    }
}
