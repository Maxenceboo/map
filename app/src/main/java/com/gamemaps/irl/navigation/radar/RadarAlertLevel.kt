package com.gamemaps.irl.navigation.radar

/** Deux niveaux d'alerte, comme la version WebGL. */
enum class RadarAlertLevel {
    /** Radar en approche (de 800 à 300 m). */
    WARNING,

    /** Radar tout proche (moins de 300 m). */
    URGENT,
}
