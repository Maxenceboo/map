package com.gamemaps.irl.map.layers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.gamemaps.irl.data.radar.RadarType

/**
 * Icônes de radar : pastille rouge cerclée de blanc.
 * - radar feu rouge : un feu tricolore ;
 * - tous les autres : un appareil photo stylisé.
 */
object RadarIconBitmap {

    private const val SIZE_PX = 72
    private val RED = Color.parseColor("#ef4444")
    private val AMBER = Color.parseColor("#f59e0b")
    private val GREEN = Color.parseColor("#22c55e")
    private val DARK = Color.parseColor("#171717")

    fun create(type: RadarType): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBadge(canvas)
        if (type == RadarType.RED_LIGHT) drawTrafficLight(canvas) else drawCamera(canvas)
        return bitmap
    }

    private fun drawBadge(canvas: Canvas) {
        val center = SIZE_PX / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawCircle(center, center, center, paint)
        paint.color = RED
        canvas.drawCircle(center, center, center * 0.82f, paint)
    }

    private fun drawCamera(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val body = RectF(SIZE_PX * 0.27f, SIZE_PX * 0.36f, SIZE_PX * 0.73f, SIZE_PX * 0.66f)
        canvas.drawRoundRect(body, 4f, 4f, paint)
        paint.color = RED
        canvas.drawCircle(SIZE_PX / 2f, body.centerY(), SIZE_PX * 0.09f, paint)
    }

    private fun drawTrafficLight(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DARK }
        val housing = RectF(SIZE_PX * 0.38f, SIZE_PX * 0.2f, SIZE_PX * 0.62f, SIZE_PX * 0.8f)
        canvas.drawRoundRect(housing, 6f, 6f, paint)
        val radius = SIZE_PX * 0.07f
        listOf(RED to 0.32f, AMBER to 0.5f, GREEN to 0.68f).forEach { (color, y) ->
            paint.color = color
            canvas.drawCircle(SIZE_PX / 2f, SIZE_PX * y, radius, paint)
        }
    }
}
