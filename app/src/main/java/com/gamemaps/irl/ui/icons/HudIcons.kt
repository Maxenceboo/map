package com.gamemaps.irl.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Icônes du HUD et des Paramètres absentes du jeu d'icônes de base de Compose (tracés Material Design, grille 24 × 24).
 * Les dessiner ici évite d'embarquer toute la bibliothèque d'icônes étendue.
 */
object HudIcons {

    val VolumeOn: ImageVector by lazy {
        icon(
            "VolumeOn",
            "M3,9v6h4l5,5V4L7,9H3z" +
                "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02z" +
                "M14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z",
        )
    }

    val VolumeOff: ImageVector by lazy {
        icon(
            "VolumeOff",
            "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63z" +
                "M19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71z" +
                "M4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3z" +
                "M12,4L9.91,6.09 12,8.18V4z",
        )
    }

    /** Viseur "ma position". */
    val Recenter: ImageVector by lazy {
        icon(
            "Recenter",
            "M12,8c-2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4z" +
                "M20.94,11c-0.46,-4.17 -3.77,-7.48 -7.94,-7.94V1h-2v2.06C6.83,3.52 3.52,6.83 3.06,11H1v2h2.06c0.46,4.17 3.77,7.48 7.94,7.94V23h2v-2.06c4.17,-0.46 7.48,-3.77 7.94,-7.94H23v-2h-2.06z" +
                "M12,19c-3.87,0 -7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7z",
        )
    }

    /** Mallette : le lieu "Travail". */
    val Work: ImageVector by lazy {
        icon(
            "Work",
            "M20,6h-4V4c0,-1.11 -0.89,-2 -2,-2h-4c-1.11,0 -2,0.89 -2,2v2H4c-1.11,0 -1.99,0.89 -1.99,2L2,19c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V8c0,-1.11 -0.89,-2 -2,-2z" +
                "M14,6h-4V4h4v2z",
        )
    }

    val Car: ImageVector by lazy {
        icon(
            "Car",
            "M18.92,6.01C18.72,5.42 18.16,5 17.5,5h-11c-0.66,0 -1.21,0.42 -1.42,1.01L3,12v8c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-1h12v1c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-8l-2.08,-5.99z" +
                "M6.5,16c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,13 6.5,13s1.5,0.67 1.5,1.5S7.33,16 6.5,16z" +
                "M17.5,16c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z" +
                "M5,11l1.5,-4.5h11L19,11H5z",
        )
    }

    /** Palette de peintre : thème de carte, couleur de carrosserie. */
    val Palette: ImageVector by lazy {
        icon(
            "Palette",
            "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8z" +
                "M6.5,12C5.67,12 5,11.33 5,10.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12z" +
                "M9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8z" +
                "M14.5,8c-0.83,0 -1.5,-0.67 -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8z" +
                "M17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z",
        )
    }

    /** Caméra : perspective de la carte. */
    val Videocam: ImageVector by lazy {
        icon("Videocam", "M17,10.5V7c0,-0.55 -0.45,-1 -1,-1H4c-0.55,0 -1,0.45 -1,1v10c0,0.55 0.45,1 1,1h12c0.55,0 1,-0.45 1,-1v-3.5l4,4v-11l-4,4z")
    }

    /** Appareil photo : les radars. */
    val Radar: ImageVector by lazy {
        icon(
            "Radar",
            "M8.8,12a3.2,3.2 0,1 0,6.4 0a3.2,3.2 0,1 0,-6.4 0z" +
                "M9,2L7.17,4H4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2H9z" +
                "M12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z",
        )
    }

    /** Feu tricolore : le trafic. */
    val Traffic: ImageVector by lazy {
        icon(
            "Traffic",
            "M20,10h-3V8.86c1.72,-0.45 3,-2 3,-3.86h-3V4c0,-0.55 -0.45,-1 -1,-1H8c-0.55,0 -1,0.45 -1,1v1H4c0,1.86 1.28,3.41 3,3.86V10H4c0,1.86 1.28,3.41 3,3.86V15H4c0,1.86 1.28,3.41 3,3.86V20c0,0.55 0.45,1 1,1h8c0.55,0 1,-0.45 1,-1v-1.14c1.72,-0.45 3,-2 3,-3.86h-3v-1.14c1.72,-0.45 3,-2 3,-3.86z" +
                "M12,19c-1.11,0 -2,-0.9 -2,-2s0.89,-2 2,-2c1.1,0 2,0.9 2,2s-0.89,2 -2,2z" +
                "M12,14c-1.11,0 -2,-0.9 -2,-2s0.89,-2 2,-2c1.1,0 2,0.9 2,2s-0.89,2 -2,2z" +
                "M12,9c-1.11,0 -2,-0.9 -2,-2 0,-1.11 0.89,-2 2,-2 1.1,0 2,0.89 2,2 0,1.1 -0.89,2 -2,2z",
        )
    }

    val Key: ImageVector by lazy {
        icon(
            "Key",
            "M12.65,10C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H17v4h4v-4h2v-4H12.65z" +
                "M7,14c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z",
        )
    }

    /** Éclair : les phares. */
    val Bolt: ImageVector by lazy { icon("Bolt", "M7,2v11h3v9l7,-12h-4l4,-8z") }

    /** Bulle : le guidage vocal. */
    val Speech: ImageVector by lazy {
        icon("Speech", "M20,2H4c-1.1,0 -1.99,0.9 -1.99,2L2,22l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2z")
    }

    val Globe: ImageVector by lazy {
        icon(
            "Globe",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z" +
                "M11,19.93c-3.95,-0.49 -7,-3.85 -7,-7.93 0,-0.62 0.08,-1.21 0.21,-1.79L9,15v1c0,1.1 0.9,2 2,2v1.93z" +
                "M17.9,17.39c-0.26,-0.81 -1,-1.39 -1.9,-1.39h-1v-3c0,-0.55 -0.45,-1 -1,-1H8v-2h2c0.55,0 1,-0.45 1,-1V7h2c1.1,0 2,-0.9 2,-2v-0.41c2.93,1.19 5,4.06 5,7.41 0,2.08 -0.8,3.97 -2.1,5.39z",
        )
    }

    val Map: ImageVector by lazy {
        icon(
            "Map",
            "M20.5,3l-0.16,0.03L15,5.1 9,3 3.36,4.9c-0.21,0.07 -0.36,0.25 -0.36,0.48V20.5c0,0.28 0.22,0.5 0.5,0.5l0.16,-0.03L9,18.9l6,2.1 5.64,-1.9c0.21,-0.07 0.36,-0.25 0.36,-0.48V3.5c0,-0.28 -0.22,-0.5 -0.5,-0.5z" +
                "M15,19l-6,-2.11V5l6,2.11V19z",
        )
    }

    /** Flèche de navigation : les itinéraires. */
    val Navigation: ImageVector by lazy { icon("Navigation", "M12,2L4.5,20.29l0.71,0.71L12,18l6.79,3 0.71,-0.71z") }

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd)
            .build()
}
