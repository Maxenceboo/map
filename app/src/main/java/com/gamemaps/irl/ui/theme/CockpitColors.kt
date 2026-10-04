package com.gamemaps.irl.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Couleurs de l'interface : surfaces bleu nuit opaques, texte clair, un seul accent
 * (le jaune des objectifs de GTA). Le tracé sur la carte a sa propre couleur (voir `MapPalette`).
 */
object CockpitColors {
    /** Fond des écrans pleins (Paramètres). */
    val Black = Color(0xFF0D1014)

    /** Surfaces du HUD posées sur la carte. */
    val Panel = Color(0xFF1F2630)

    /** Boutons et pastilles posés sur une surface. */
    val PanelRaised = Color(0xFF2E3744)

    /** Séparateurs fins. */
    val Border = Color(0xFF333D4A)

    val Text = Color(0xFFF4F6F8)
    val TextMuted = Color(0xFF98A2AE)

    val Accent = Color(0xFFFFC533)

    /** Texte et icônes posés sur l'accent. */
    val OnAccent = Color(0xFF1A1300)

    val Danger = Color(0xFFE5484D)
    val Warning = Color(0xFFFF9F1C)
    val GpsGood = Color(0xFF3DD68C)
    val GpsFair = Color(0xFFF2C94C)
}
