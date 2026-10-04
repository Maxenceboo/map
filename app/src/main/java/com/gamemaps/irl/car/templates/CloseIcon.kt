package com.gamemaps.irl.car.templates

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat

/** Croix rouge des boutons "annuler le trajet" d'Android Auto, dessinée une seule fois. */
object CloseIcon {

    private const val SIZE_PX = 96

    val icon: CarIcon by lazy {
        // Dessinée en blanc : c'est la teinte (rouge de la voiture) qui lui donne sa couleur.
        CarIcon.Builder(IconCompat.createWithBitmap(render())).setTint(CarColor.RED).build()
    }

    private fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = SIZE_PX * 0.14f
            strokeCap = Paint.Cap.ROUND
        }
        val near = SIZE_PX * 0.22f
        val far = SIZE_PX * 0.78f
        Canvas(bitmap).apply {
            drawLine(near, near, far, far, paint)
            drawLine(far, near, near, far, paint)
        }
        return bitmap
    }
}
