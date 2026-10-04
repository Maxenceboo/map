package com.gamemaps.irl.car.mapping

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.car.app.model.CarIcon
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.drawable.IconCompat
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.ui.hud.ManeuverArrowShape

/**
 * Icône de manœuvre pour Android Auto : les mêmes flèches que le HUD du téléphone
 * ([ManeuverArrowShape], grille 24 × 24), dessinées ici dans une image.
 * Les icônes sont mises en cache : on ne redessine pas à chaque position GPS.
 */
object ManeuverIconFactory {

    private const val SIZE_PX = 192

    /** Une case de la grille 24 × 24, en pixels. */
    private const val UNIT = SIZE_PX / 24f

    /** Jaune d'accent du HUD. */
    private val ACCENT = Color.parseColor("#FFC533")

    private val cache = mutableMapOf<Pair<ManeuverType, Int?>, CarIcon>()

    /** @param roundaboutExit numéro de sortie, écrit au centre du rond-point. */
    fun icon(maneuver: ManeuverType, roundaboutExit: Int? = null): CarIcon {
        val exit = roundaboutExit.takeIf { maneuver == ManeuverType.ROUNDABOUT }
        return cache.getOrPut(maneuver to exit) {
            CarIcon.Builder(IconCompat.createWithBitmap(render(maneuver, exit))).build()
        }
    }

    private fun render(maneuver: ManeuverType, exit: Int?): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        when (maneuver) {
            ManeuverType.ROUNDABOUT -> drawRoundabout(canvas, exit)
            ManeuverType.ARRIVE -> drawPin(canvas)
            else -> {
                val shape = ManeuverArrowShape.of(maneuver)
                // Virage à droite : la même flèche, retournée horizontalement.
                if (shape.mirrored) canvas.scale(-1f, 1f, SIZE_PX / 2f, SIZE_PX / 2f)
                drawArrow(canvas, shape.points)
            }
        }
        return bitmap
    }

    private fun drawArrow(canvas: Canvas, points: List<Offset>) {
        val line = Path().apply {
            points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x * UNIT, p.y * UNIT) else lineTo(p.x * UNIT, p.y * UNIT) }
        }
        canvas.drawPath(line, stroke(3.2f))
        drawHead(canvas, from = points[points.lastIndex - 1], to = points.last())
    }

    /** Pointe triangulaire au bout de la flèche, dans le sens du dernier segment. */
    private fun drawHead(canvas: Canvas, from: Offset, to: Offset) {
        val direction = (to - from).let { it / it.getDistance() }
        val side = Offset(-direction.y, direction.x)
        val tip = to + direction * 4f
        val left = to + side * 4f
        val right = to - side * 4f
        val head = Path().apply {
            moveTo(tip.x * UNIT, tip.y * UNIT)
            lineTo(left.x * UNIT, left.y * UNIT)
            lineTo(right.x * UNIT, right.y * UNIT)
            close()
        }
        canvas.drawPath(head, fill())
        canvas.drawPath(head, stroke(1.2f))
    }

    /** Anneau, entrée par le bas, sortie fléchée vers le haut, numéro de sortie au centre. */
    private fun drawRoundabout(canvas: Canvas, exit: Int?) {
        canvas.drawCircle(12f * UNIT, 13f * UNIT, 5.5f * UNIT, stroke(2.6f))
        canvas.drawLine(12f * UNIT, 22.5f * UNIT, 12f * UNIT, 19f * UNIT, stroke(2.6f))
        drawHead(canvas, from = Offset(12f, 7f), to = Offset(12f, 5.5f))
        if (exit != null) {
            val text = fill().apply {
                textSize = 7f * UNIT
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText(exit.toString(), 12f * UNIT, 13f * UNIT - (text.descent() + text.ascent()) / 2f, text)
        }
    }

    /** Repère d'arrivée : une goutte avec un trou au centre. */
    private fun drawPin(canvas: Canvas) {
        val pin = Path().apply {
            moveTo(12f * UNIT, 22f * UNIT)
            cubicTo(12f * UNIT, 22f * UNIT, 5f * UNIT, 14f * UNIT, 5f * UNIT, 9f * UNIT)
            cubicTo(5f * UNIT, 5.1f * UNIT, 8.1f * UNIT, 2f * UNIT, 12f * UNIT, 2f * UNIT)
            cubicTo(15.9f * UNIT, 2f * UNIT, 19f * UNIT, 5.1f * UNIT, 19f * UNIT, 9f * UNIT)
            cubicTo(19f * UNIT, 14f * UNIT, 12f * UNIT, 22f * UNIT, 12f * UNIT, 22f * UNIT)
            close()
            addCircle(12f * UNIT, 9f * UNIT, 2.6f * UNIT, Path.Direction.CW)
            fillType = Path.FillType.EVEN_ODD
        }
        canvas.drawPath(pin, fill())
    }

    private fun fill() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ACCENT
        style = Paint.Style.FILL
    }

    private fun stroke(widthUnits: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ACCENT
        style = Paint.Style.STROKE
        strokeWidth = widthUnits * UNIT
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
}
