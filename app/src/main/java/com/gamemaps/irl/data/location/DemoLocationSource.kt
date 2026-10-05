package com.gamemaps.irl.data.location

import com.gamemaps.irl.core.geo.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Position de démonstration : le véhicule remonte les Champs-Élysées, à Paris, sans bouger.
 * Sert à présenter l'app (captures d'écran, démonstration) sans montrer où l'on se trouve.
 */
class DemoLocationSource(private val clock: () -> Long = System::currentTimeMillis) : LocationSource {

    override fun fixes(): Flow<GpsFix> = flow {
        while (true) {
            emit(GpsFix(POSITION, SPEED_METERS_PER_SECOND, BEARING_DEGREES, accuracyMeters = 4f, timeMillis = clock()))
            delay(INTERVAL_MS)
        }
    }

    companion object {
        /** Avenue des Champs-Élysées, entre la Concorde et le rond-point. */
        val POSITION = LatLng(48.8672, 2.3156)

        /** Vers l'Arc de Triomphe. */
        const val BEARING_DEGREES = 296f

        /** 28 km/h : sous la limitation parisienne, pour ne pas déclencher l'alerte d'excès. */
        const val SPEED_METERS_PER_SECOND = 7.8f
        private const val INTERVAL_MS = 1_000L
    }
}
