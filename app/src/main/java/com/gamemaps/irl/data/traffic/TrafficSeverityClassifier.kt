package com.gamemaps.irl.data.traffic

/**
 * Décide si une portion ralentie est un simple ralentissement ou un vrai bouchon
 * (mêmes seuils que la version WebGL).
 */
object TrafficSeverityClassifier {

    private const val JAM_SPEED_KMH = 18.0
    private const val JAM_DELAY_SECONDS = 120.0
    private const val JAM_MAGNITUDE = 2

    /**
     * @param speedKmh vitesse réelle sur la portion, null si inconnue.
     * @param magnitude échelle TomTom : 0 inconnu, 1 mineur, 2 modéré, 3 majeur, 4 route fermée.
     */
    fun classify(speedKmh: Double?, delaySeconds: Double, magnitude: Int): TrafficSeverity {
        val isJam = (speedKmh != null && speedKmh <= JAM_SPEED_KMH) ||
            delaySeconds >= JAM_DELAY_SECONDS ||
            magnitude >= JAM_MAGNITUDE
        return if (isJam) TrafficSeverity.JAM else TrafficSeverity.SLOW
    }
}
