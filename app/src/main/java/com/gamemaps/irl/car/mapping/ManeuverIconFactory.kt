package com.gamemaps.irl.car.mapping

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.navigation.instructions.ManeuverGlyphs

/**
 * Icône de manœuvre pour Android Auto, dessinée à partir de la même flèche que le HUD téléphone.
 * Les icônes sont mises en cache : on ne redessine pas à chaque position GPS.
 */
object ManeuverIconFactory {

    private const val SIZE_PX = 128
    private val cache = mutableMapOf<ManeuverType, CarIcon>()

    fun icon(maneuver: ManeuverType): CarIcon = cache.getOrPut(maneuver) {
        CarIcon.Builder(IconCompat.createWithBitmap(render(ManeuverGlyphs.glyph(maneuver)))).build()
    }

    private fun render(glyph: String): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = SIZE_PX * 0.8f
            textAlign = Paint.Align.CENTER
        }
        // Centre vertical : la ligne de base est décalée de la moitié de la hauteur du texte.
        val baseline = SIZE_PX / 2f - (paint.descent() + paint.ascent()) / 2f
        Canvas(bitmap).drawText(glyph, SIZE_PX / 2f, baseline, paint)
        return bitmap
    }
}
