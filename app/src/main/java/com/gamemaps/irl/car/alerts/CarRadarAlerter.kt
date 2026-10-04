package com.gamemaps.irl.car.alerts

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.model.Alert
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarText
import androidx.car.app.versioning.CarAppApiLevels
import androidx.core.graphics.drawable.IconCompat
import com.gamemaps.irl.data.radar.RadarType
import com.gamemaps.irl.map.layers.RadarIconBitmap
import com.gamemaps.irl.navigation.radar.RadarAlert

/**
 * Affiche les alertes radar sur l'écran de la voiture.
 *
 * - Hôte récent (API Car App ≥ 5) : vraie alerte Android Auto par-dessus la carte (icône, type, distance).
 * - Hôte plus ancien : simple message court (toast).
 */
class CarRadarAlerter(private val carContext: CarContext) {

    private val policy = CarRadarAlertPolicy()
    private val appManager = carContext.getCarService(AppManager::class.java)
    private val supportsAlerts = carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_5
    private val icons = mutableMapOf<RadarType, CarIcon>()

    fun onAlert(alert: RadarAlert?) {
        when (val decision = policy.onAlert(alert)) {
            is CarRadarAlertPolicy.Decision.Show -> show(decision)
            is CarRadarAlertPolicy.Decision.Dismiss -> if (supportsAlerts) appManager.dismissAlert(decision.alertId)
            CarRadarAlertPolicy.Decision.Nothing -> Unit
        }
    }

    private fun show(decision: CarRadarAlertPolicy.Decision.Show) {
        if (!supportsAlerts) {
            CarToast.makeText(carContext, "${decision.title} ${decision.subtitle}", CarToast.LENGTH_LONG).show()
            return
        }
        appManager.showAlert(
            Alert.Builder(decision.alertId, CarText.create(decision.title), DURATION_MILLIS)
                .setSubtitle(CarText.create(decision.subtitle))
                .setIcon(iconFor(decision.alert))
                .build(),
        )
    }

    private fun iconFor(alert: RadarAlert): CarIcon = icons.getOrPut(alert.radar.type) {
        CarIcon.Builder(IconCompat.createWithBitmap(RadarIconBitmap.create(alert.radar.type))).build()
    }

    private companion object {
        /** L'alerte reste au plus 15 s ; elle est retirée plus tôt si le radar est dépassé. */
        const val DURATION_MILLIS = 15_000L
    }
}
