package com.gamemaps.irl.map.vehicle3d

/** Rôle d'une pièce du véhicule : détermine sa couleur (voir [VehiclePalette]). */
enum class PartRole {
    /** Carrosserie : prend la couleur choisie par l'utilisateur. */
    BODY,

    /** Vitrage teinté. */
    GLASS,

    /** Pneus. */
    TIRE,

    /** Pièces sombres : aileron, pare-chocs, barres de toit, châssis. */
    TRIM,

    /** Optiques avant. */
    HEADLIGHT,

    /** Feux arrière. */
    TAILLIGHT,
}
