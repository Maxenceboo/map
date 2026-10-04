package com.gamemaps.irl.data.location

/**
 * Lisse la vitesse affichée (reprise de la version WebGL) :
 * - sous [stopThresholdMps] (~3 km/h), le véhicule est arrêté : vitesse forcée à 0 (pas de "vitesse fantôme") ;
 * - sinon, moyenne glissante pour éviter que le compteur saute à chaque mesure.
 */
class SpeedSmoother(
    private val stopThresholdMps: Float = 0.83f,
    private val newWeight: Float = 0.35f,
) {
    private var smoothed = 0f

    fun smooth(fix: GpsFix): GpsFix {
        val raw = fix.speedMetersPerSecond ?: return fix
        smoothed = when {
            raw < stopThresholdMps -> 0f
            smoothed == 0f -> raw
            else -> smoothed * (1 - newWeight) + raw * newWeight
        }
        return fix.copy(speedMetersPerSecond = smoothed)
    }
}
