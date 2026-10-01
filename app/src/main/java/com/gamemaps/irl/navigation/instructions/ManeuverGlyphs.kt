package com.gamemaps.irl.navigation.instructions

import com.gamemaps.irl.data.routing.ManeuverType

/**
 * Flèche texte pour chaque manœuvre. Utilisée par le HUD téléphone et pour
 * dessiner l'icône de manœuvre envoyée à Android Auto.
 * (Des icônes vectorielles dédiées pourront remplacer ces caractères plus tard.)
 */
object ManeuverGlyphs {

    fun glyph(type: ManeuverType): String = when (type) {
        ManeuverType.DEPART, ManeuverType.STRAIGHT, ManeuverType.MERGE -> "↑"
        ManeuverType.SLIGHT_LEFT, ManeuverType.RAMP_LEFT, ManeuverType.FORK_LEFT -> "↖"
        ManeuverType.LEFT -> "←"
        ManeuverType.SHARP_LEFT -> "↙"
        ManeuverType.SLIGHT_RIGHT, ManeuverType.RAMP_RIGHT, ManeuverType.FORK_RIGHT -> "↗"
        ManeuverType.RIGHT -> "→"
        ManeuverType.SHARP_RIGHT -> "↘"
        ManeuverType.UTURN -> "↶"
        ManeuverType.ROUNDABOUT -> "↻"
        ManeuverType.ARRIVE -> "◎"
    }
}
