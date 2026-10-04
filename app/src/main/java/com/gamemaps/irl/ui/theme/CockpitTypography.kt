package com.gamemaps.irl.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Styles de texte : la police du système partout, la hiérarchie vient de la taille et de la graisse.
 * "tnum" donne des chiffres de largeur fixe : la vitesse et les distances ne bougent pas en changeant.
 */
object CockpitTypography {
    private const val TABULAR = "tnum"

    /** Vitesse dans le compteur. */
    val Speed = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, fontFeatureSettings = TABULAR)

    /** Grande valeur : distance avant la manœuvre, heure d'arrivée, titre d'écran. */
    val Distance = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, fontFeatureSettings = TABULAR)

    /** Valeur secondaire et libellé de bouton. */
    val Metric = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFeatureSettings = TABULAR)

    /** Nom de rue, ligne de liste. */
    val Street = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp)

    /** Texte d'accompagnement. */
    val Caption = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp)
}
