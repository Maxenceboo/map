package com.gamemaps.irl.map.layers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.gamemaps.irl.map.theme.MapPalette

/** Épingle de destination aux couleurs de l'itinéraire, pointe vers le bas. */
object DestinationPinBitmap {

    private const val WIDTH_PX = 72
    private const val HEIGHT_PX = 96

    fun create(palette: MapPalette): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH_PX, HEIGHT_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = WIDTH_PX / 2f
        val radius = WIDTH_PX * 0.42f

        val pin = Path().apply {
            addCircle(center, radius + 2, radius, Path.Direction.CW)
            moveTo(center - radius * 0.6f, radius * 1.6f)
            lineTo(center, HEIGHT_PX - 2f)
            lineTo(center + radius * 0.6f, radius * 1.6f)
            close()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor(palette.route)
        canvas.drawPath(pin, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.WHITE
        canvas.drawPath(pin, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(center, radius + 2, radius * 0.38f, paint)
        return bitmap
    }
}
