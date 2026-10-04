package com.gamemaps.irl.map.layers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/** Icône de radar : pastille rouge cerclée de blanc, avec un appareil photo stylisé. */
object RadarIconBitmap {

    private const val SIZE_PX = 72
    private val RED = Color.parseColor("#ef4444")

    fun create(): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = SIZE_PX / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.WHITE
        canvas.drawCircle(center, center, center, paint)
        paint.color = RED
        canvas.drawCircle(center, center, center * 0.82f, paint)

        // Boîtier + objectif.
        paint.color = Color.WHITE
        val body = RectF(SIZE_PX * 0.27f, SIZE_PX * 0.36f, SIZE_PX * 0.73f, SIZE_PX * 0.66f)
        canvas.drawRoundRect(body, 4f, 4f, paint)
        paint.color = RED
        canvas.drawCircle(center, body.centerY(), SIZE_PX * 0.09f, paint)
        return bitmap
    }
}
