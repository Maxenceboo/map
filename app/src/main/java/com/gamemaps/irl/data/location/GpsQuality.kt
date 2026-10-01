package com.gamemaps.irl.data.location

/** Qualité du signal GPS, affichée par la pastille du HUD (vert / jaune / rouge). */
enum class GpsQuality {
    GOOD,
    FAIR,
    WEAK,
    SEARCHING;

    companion object {
        fun from(fix: GpsFix?): GpsQuality = when {
            fix == null -> SEARCHING
            fix.accuracyMeters < 10f -> GOOD
            fix.accuracyMeters < 30f -> FAIR
            else -> WEAK
        }
    }
}
