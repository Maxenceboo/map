package com.gamemaps.irl.car.alerts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.model.Alert
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarText
import androidx.car.app.versioning.CarAppApiLevels
import androidx.core.graphics.drawable.IconCompat
import com.gamemaps.irl.navigation.radar.RadarAlert

/**
 * Affiche l'alerte "zone de danger" sur l'écran de la voiture.
 *
 * - Hôte récent (API Car App ≥ 5) : vraie alerte Android Auto par-dessus la carte.
 * - Hôte plus ancien : simple message court (toast).
 */
class CarRadarAlerter(private val carContext: CarContext) {

    private val policy = CarRadarAlertPolicy()
    private val appManager = carContext.getCarService(AppManager::class.java)
    private val supportsAlerts = carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_5
    private val icon: CarIcon by lazy { CarIcon.Builder(IconCompat.createWithBitmap(warningTriangle())).build() }

    fun onAlert(alert: RadarAlert?) {
        when (val decision = policy.onAlert(alert)) {
            is CarRadarAlertPolicy.Decision.Show -> show(decision)
            is CarRadarAlertPolicy.Decision.Dismiss -> if (supportsAlerts) appManager.dismissAlert(decision.alertId)
            CarRadarAlertPolicy.Decision.Nothing -> Unit
        }
    }

    private fun show(decision: CarRadarAlertPolicy.Decision.Show) {
        if (!supportsAlerts) {
            CarToast.makeText(carContext, "${decision.title} · ${decision.subtitle}", CarToast.LENGTH_LONG).show()
            return
        }
        appManager.showAlert(
            Alert.Builder(decision.alertId, CarText.create(decision.title), DURATION_MILLIS)
                .setSubtitle(CarText.create(decision.subtitle))
                .setIcon(icon)
                .build(),
        )
    }

    /** Triangle d'avertissement orange avec un point d'exclamation. */
    private fun warningTriangle(): Bitmap {
        val size = 96f
        val bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val triangle = Path().apply {
            moveTo(size / 2, size * 0.1f)
            lineTo(size * 0.94f, size * 0.88f)
            lineTo(size * 0.06f, size * 0.88f)
            close()
        }
        canvas.drawPath(triangle, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF9F1C") })
        val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1300")
            strokeWidth = size * 0.09f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(size / 2, size * 0.38f, size / 2, size * 0.62f, mark)
        canvas.drawCircle(size / 2, size * 0.76f, size * 0.05f, mark)
        return bitmap
    }

    private companion object {
        /** L'alerte reste au plus 15 s ; elle est retirée plus tôt si on sort de la zone. */
        const val DURATION_MILLIS = 15_000L
    }
}
