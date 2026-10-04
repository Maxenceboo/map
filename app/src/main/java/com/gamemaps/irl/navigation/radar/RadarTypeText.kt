package com.gamemaps.irl.navigation.radar

import com.gamemaps.irl.data.radar.RadarType

/** Libellés d'un type de radar, pour le bandeau du HUD et pour la voix. */
object RadarTypeText {

    fun banner(type: RadarType): String = when (type) {
        RadarType.SPEED -> "RADAR"
        RadarType.RED_LIGHT -> "RADAR FEU ROUGE"
        RadarType.DISCRIMINANT -> "RADAR DISCRIMINANT"
        RadarType.SECTION -> "RADAR TRONÇON"
        RadarType.LEVEL_CROSSING -> "PASSAGE À NIVEAU"
    }

    fun spoken(type: RadarType): String = when (type) {
        RadarType.SPEED -> "Radar"
        RadarType.RED_LIGHT -> "Radar feu rouge"
        RadarType.DISCRIMINANT -> "Radar discriminant"
        RadarType.SECTION -> "Radar tronçon"
        RadarType.LEVEL_CROSSING -> "Radar de passage à niveau"
    }
}
