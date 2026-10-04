package com.gamemaps.irl.ui.hud

import androidx.compose.ui.geometry.Offset
import com.gamemaps.irl.data.routing.ManeuverType

/**
 * Tracé d'une flèche de manœuvre sur une grille 24 × 24 (x vers la droite, y vers le bas).
 * La flèche part du bas (là où est le conducteur) et finit par la pointe.
 *
 * @property points sommets de la ligne, dessinés pour un virage à gauche.
 * @property mirrored true pour la version "à droite" (symétrie horizontale).
 */
data class ManeuverArrowShape(val points: List<Offset>, val mirrored: Boolean = false) {

    companion object {
        private val STRAIGHT = listOf(Offset(12f, 21f), Offset(12f, 7f))
        private val SLIGHT = listOf(Offset(14f, 21f), Offset(14f, 14f), Offset(8.5f, 8.5f))
        private val TURN = listOf(Offset(16f, 21f), Offset(16f, 10f), Offset(8f, 10f))
        private val SHARP = listOf(Offset(16f, 21f), Offset(16f, 7f), Offset(10f, 13f))

        // Demi-tour par la gauche (conduite à droite) : on monte, on contourne, on redescend.
        private val UTURN = listOf(Offset(17f, 21f), Offset(17f, 9f), Offset(15f, 6f), Offset(10f, 6f), Offset(8f, 9f), Offset(8f, 14f))

        /** Le rond-point et l'arrivée ont leur propre dessin (voir [ManeuverIcon]). */
        fun of(type: ManeuverType): ManeuverArrowShape = when (type) {
            ManeuverType.DEPART, ManeuverType.STRAIGHT, ManeuverType.MERGE,
            ManeuverType.ROUNDABOUT, ManeuverType.ARRIVE -> ManeuverArrowShape(STRAIGHT)
            ManeuverType.SLIGHT_LEFT, ManeuverType.RAMP_LEFT, ManeuverType.FORK_LEFT -> ManeuverArrowShape(SLIGHT)
            ManeuverType.SLIGHT_RIGHT, ManeuverType.RAMP_RIGHT, ManeuverType.FORK_RIGHT -> ManeuverArrowShape(SLIGHT, mirrored = true)
            ManeuverType.LEFT -> ManeuverArrowShape(TURN)
            ManeuverType.RIGHT -> ManeuverArrowShape(TURN, mirrored = true)
            ManeuverType.SHARP_LEFT -> ManeuverArrowShape(SHARP)
            ManeuverType.SHARP_RIGHT -> ManeuverArrowShape(SHARP, mirrored = true)
            ManeuverType.UTURN -> ManeuverArrowShape(UTURN)
        }
    }
}
