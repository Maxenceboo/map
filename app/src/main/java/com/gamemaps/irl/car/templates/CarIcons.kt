package com.gamemaps.irl.car.templates

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.core.graphics.PathParser
import androidx.core.graphics.drawable.IconCompat

/**
 * Icônes des boutons d'Android Auto, dessinées une seule fois à partir de tracés
 * Material Design (grille 24 × 24). Dessinées en blanc, puis teintées : jaune dans les listes, blanc ou rouge pour les boutons posés sur la carte.
 */
object CarIcons {

    private const val SIZE_PX = 96

    private const val SEARCH_PATH =
        "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5z" +
            "M9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"

    /** Jaune d'accent de l'app, comme les icônes du téléphone. */
    private val ACCENT = CarColor.createCustom(Color.parseColor("#FFC533"), Color.parseColor("#FFC533"))

    /** Maison : le trajet vers le domicile. */
    val home: CarIcon by lazy { fromPath("M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z") }

    /** Mallette : lance le trajet vers le travail. */
    val work: CarIcon by lazy {
        fromPath(
            "M20,6h-4V4c0,-1.11 -0.89,-2 -2,-2h-4c-1.11,0 -2,0.89 -2,2v2H4c-1.11,0 -1.99,0.89 -1.99,2L2,19c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V8c0,-1.11 -0.89,-2 -2,-2z" +
                "M14,6h-4V4h4v2z",
        )
    }

    /** Loupe : la recherche de destination. */
    val search: CarIcon by lazy { fromPath(SEARCH_PATH) }

    /** Étoile : un favori. */
    val star: CarIcon by lazy { fromPath("M12,17.27L18.18,21l-1.64,-7.03L22,9.24l-7.19,-0.61L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21z") }

    /** Loupe blanche : bouton de recherche posé sur la carte. */
    val searchButton: CarIcon by lazy { fromPath(SEARCH_PATH, CarColor.DEFAULT) }

    /** Haut-parleur : le son est actif. */
    val volumeOn: CarIcon by lazy {
        fromPath(
            "M3,9v6h4l5,5V4L7,9H3z" +
                "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02z" +
                "M14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z",
            CarColor.DEFAULT,
        )
    }

    /** Haut-parleur barré, en rouge : le son est coupé. */
    val volumeOff: CarIcon by lazy {
        fromPath(
            "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63z" +
                "M19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71z" +
                "M4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3z" +
                "M12,4L9.91,6.09 12,8.18V4z",
            CarColor.RED,
        )
    }

    /** Croix : annule le trajet. */
    val close: CarIcon by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = SIZE_PX * 0.14f
            strokeCap = Paint.Cap.ROUND
        }
        val near = SIZE_PX * 0.22f
        val far = SIZE_PX * 0.78f
        val bitmap = newBitmap()
        Canvas(bitmap).apply {
            drawLine(near, near, far, far, paint)
            drawLine(far, near, near, far, paint)
        }
        CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).setTint(CarColor.RED).build()
    }

    private fun fromPath(pathData: String, tint: CarColor = ACCENT): CarIcon {
        val path: Path = PathParser.createPathFromPathData(pathData).apply {
            fillType = Path.FillType.EVEN_ODD
            // De la grille 24 × 24 à la taille de l'image.
            transform(Matrix().apply { setScale(SIZE_PX / 24f, SIZE_PX / 24f) })
        }
        val bitmap = newBitmap()
        Canvas(bitmap).drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).setTint(tint).build()
    }

    private fun newBitmap(): Bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
}
