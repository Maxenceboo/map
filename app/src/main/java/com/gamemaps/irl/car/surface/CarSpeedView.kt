package com.gamemaps.irl.car.surface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.gamemaps.irl.data.speedlimit.SpeedLimit
import com.gamemaps.irl.navigation.SpeedingDetector

/**
 * Compteur de vitesse dessiné par-dessus la carte de la voiture (Android Auto n'en fournit pas) :
 * le même que sur le téléphone, un cercle avec la vitesse, et le panneau de limitation accroché en haut à droite.
 * Rouge au-dessus d'une limitation renseignée, orange au-dessus d'une limitation estimée.
 */
class CarSpeedView(context: Context) : View(context) {

    private var speedKmh = 0
    private var limit: SpeedLimit? = null

    private val dp = resources.displayMetrics.density
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    fun show(speedKmh: Int, limit: SpeedLimit?) {
        if (speedKmh == this.speedKmh && limit == this.limit) return
        this.speedKmh = speedKmh
        this.limit = limit
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = (SIZE_DP * dp).toInt()
        setMeasuredDimension(side, side)
    }

    override fun onDraw(canvas: Canvas) {
        val limit = limit
        val over = SpeedingDetector.isSpeeding(speedKmh, limit?.kmh)
        val background = when {
            !over -> PANEL
            limit?.estimated == true -> WARNING
            else -> DANGER
        }
        val onBackground = if (background == WARNING) ON_ACCENT else TEXT

        // Le cercle occupe le bas gauche de la vue ; le panneau dépasse en haut à droite.
        val radius = GAUGE_RADIUS_DP * dp
        val cx = radius
        val cy = height - radius
        fill.color = background
        canvas.drawCircle(cx, cy, radius, fill)

        text.color = onBackground
        text.textSize = 30 * dp
        canvas.drawText(speedKmh.toString(), cx, cy + 6 * dp, text)
        text.color = if (over) onBackground else TEXT_MUTED
        text.textSize = 12 * dp
        text.typeface = Typeface.DEFAULT
        canvas.drawText("km/h", cx, cy + 24 * dp, text)
        text.typeface = Typeface.DEFAULT_BOLD

        if (limit != null) drawSign(canvas, limit)
    }

    /** Panneau rond : bordure rouge, ou grise avec "~" si la limitation est estimée. */
    private fun drawSign(canvas: Canvas, limit: SpeedLimit) {
        val radius = SIGN_RADIUS_DP * dp
        val cx = width - radius
        val cy = radius
        fill.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius, fill)
        ring.color = if (limit.estimated) SIGN_GREY else SIGN_RED
        ring.strokeWidth = 5 * dp
        canvas.drawCircle(cx, cy, radius - ring.strokeWidth / 2, ring)

        val label = if (limit.estimated) "~${limit.kmh}" else limit.kmh.toString()
        text.color = Color.BLACK
        text.textSize = (if (label.length >= 3) 13 else 17) * dp
        canvas.drawText(label, cx, cy - (text.descent() + text.ascent()) / 2, text)
    }

    private companion object {
        const val SIZE_DP = 112
        const val GAUGE_RADIUS_DP = 44
        const val SIGN_RADIUS_DP = 24

        // Mêmes couleurs que le HUD du téléphone (voir CockpitColors).
        val PANEL = Color.parseColor("#1F2630")
        val TEXT = Color.parseColor("#F4F6F8")
        val TEXT_MUTED = Color.parseColor("#98A2AE")
        val ON_ACCENT = Color.parseColor("#1A1300")
        val DANGER = Color.parseColor("#E5484D")
        val WARNING = Color.parseColor("#FF9F1C")
        val SIGN_RED = Color.parseColor("#D9121A")
        val SIGN_GREY = Color.parseColor("#8A929C")
    }
}
