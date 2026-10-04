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
 * Material Design (grille 24 × 24). Blanches : la voiture leur applique sa teinte.
 */
object CarIcons {

    private const val SIZE_PX = 96

    /** Maison : lance le trajet vers le domicile. */
    val home: CarIcon by lazy { fromPath("M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z") }

    /** Mallette : lance le trajet vers le travail. */
    val work: CarIcon by lazy {
        fromPath(
            "M20,6h-4V4c0,-1.11 -0.89,-2 -2,-2h-4c-1.11,0 -2,0.89 -2,2v2H4c-1.11,0 -1.99,0.89 -1.99,2L2,19c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V8c0,-1.11 -0.89,-2 -2,-2z" +
                "M14,6h-4V4h4v2z",
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

    private fun fromPath(pathData: String): CarIcon {
        val path: Path = PathParser.createPathFromPathData(pathData).apply {
            fillType = Path.FillType.EVEN_ODD
            // De la grille 24 × 24 à la taille de l'image.
            transform(Matrix().apply { setScale(SIZE_PX / 24f, SIZE_PX / 24f) })
        }
        val bitmap = newBitmap()
        Canvas(bitmap).drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).setTint(CarColor.DEFAULT).build()
    }

    private fun newBitmap(): Bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
}
