package com.gamemaps.irl.data.settings

/** Angle de vue de la carte en conduite. */
enum class Perspective(val label: String) {
    /** Caméra inclinée derrière le véhicule (cockpit, 55°). */
    COCKPIT_3D("3D (cockpit)"),

    /** Vue de dessus, nord en haut du véhicule. */
    TOP_DOWN_2D("2D (vue de dessus)"),
}
