package com.gamemaps.irl.map

import android.animation.ValueAnimator
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import com.gamemaps.irl.core.geo.FixInterpolator
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.data.location.GpsFix

/**
 * Fait glisser le véhicule d'une position GPS à la suivante.
 *
 * Le GPS donne environ une position par seconde : sans animation, le véhicule sauterait d'un point
 * à l'autre. Ici, chaque nouvelle position devient une cible, rejointe en ligne droite sur la durée
 * qui sépare deux mesures ; [onFrame] reçoit les positions intermédiaires (véhicule + caméra).
 */
class VehicleAnimator(private val onFrame: (GpsFix) -> Unit) {

    /** Dernière position réellement affichée. */
    private var shown: GpsFix? = null
    private var lastTargetTimeMillis = 0L
    private var lastFrameMillis = 0L
    private var animator: ValueAnimator? = null

    fun moveTo(target: GpsFix) {
        val from = shown
        val duration = (target.timeMillis - lastTargetTimeMillis).coerceIn(MIN_DURATION_MS, MAX_DURATION_MS)
        lastTargetTimeMillis = target.timeMillis
        animator?.cancel()

        // Première position, ou saut trop grand pour être un déplacement (tunnel, position de démonstration) : on s'y place directement.
        if (from == null || GeoMath.distanceMeters(from.position, target.position) > JUMP_METERS) {
            show(target)
            return
        }
        if (from.position == target.position && from.bearingDegrees == target.bearingDegrees) {
            show(target)
            return
        }
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                // On limite le nombre d'images : redessiner le véhicule plus de 30 fois par seconde ne se voit pas.
                val now = SystemClock.uptimeMillis()
                if (fraction < 1f && now - lastFrameMillis < FRAME_INTERVAL_MS) return@addUpdateListener
                lastFrameMillis = now
                show(FixInterpolator.between(from, target, fraction))
            }
            start()
        }
    }

    private fun show(fix: GpsFix) {
        shown = fix
        onFrame(fix)
    }

    private companion object {
        const val MIN_DURATION_MS = 300L
        const val MAX_DURATION_MS = 1_500L
        const val FRAME_INTERVAL_MS = 33L
        const val JUMP_METERS = 150.0
    }
}
