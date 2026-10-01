package com.gamemaps.irl.map.layers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.gamemaps.irl.map.theme.MapPalette

/**
 * Dessine la flèche de navigation façon radar GTA, pointant vers le haut (nord).
 * C'est un marqueur 2D provisoire : la voiture 3D (Filament) le remplacera.
 */
object VehicleArrowBitmap {

    private const val SIZE_PX = 96

    fun create(palette: MapPalette): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path = arrowPath(SIZE_PX.toFloat())

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor(palette.vehicle)
        }
        val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = SIZE_PX * 0.06f
            strokeJoin = Paint.Join.ROUND
            color = Color.parseColor(palette.vehicleOutline)
        }
        canvas.drawPath(path, fill)
        canvas.drawPath(path, outline)
        return bitmap
    }

    /** Pointe en haut, deux ailes en bas, encoche au centre. */
    private fun arrowPath(size: Float): Path {
        val margin = size * 0.1f
        return Path().apply {
            moveTo(size / 2, margin)
            lineTo(size - margin, size - margin)
            lineTo(size / 2, size * 0.7f)
            lineTo(margin, size - margin)
            close()
        }
    }
}
