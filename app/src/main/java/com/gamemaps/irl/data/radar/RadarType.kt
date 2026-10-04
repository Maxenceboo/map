package com.gamemaps.irl.data.radar

/** Catégories de radars automatiques français (cahier des charges §6.1). */
enum class RadarType {
    /** Radar fixe classique (vitesse instantanée). */
    SPEED,

    /** Radar de franchissement de feu rouge. */
    RED_LIGHT,

    /** Radar discriminant (distingue poids lourds et véhicules légers). */
    DISCRIMINANT,

    /** Radar de tronçon (vitesse moyenne entre deux points). */
    SECTION,

    /** Radar de passage à niveau. */
    LEVEL_CROSSING,
}
