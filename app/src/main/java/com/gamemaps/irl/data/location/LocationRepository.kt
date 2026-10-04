package com.gamemaps.irl.data.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Point d'accès unique à la position, partagé entre le téléphone et Android Auto.
 *
 * Chaque mesure brute passe par trois filtres, dans cet ordre :
 * 1. [SpeedSmoother] : vitesse lissée, 0 à l'arrêt ;
 * 2. [StationaryPositionFilter] : position figée à l'arrêt (s'appuie sur la vitesse lissée) ;
 * 3. [HeadingStabilizer] : cap conservé à l'arrêt.
 *
 * [start] doit être appelé une fois la permission accordée ; il est sans effet si déjà démarré.
 */
class LocationRepository(
    private val source: LocationSource,
    private val scope: CoroutineScope,
) {
    private val _fixes = MutableStateFlow<GpsFix?>(null)
    val fixes: StateFlow<GpsFix?> = _fixes.asStateFlow()

    private val speedSmoother = SpeedSmoother()
    private val stationaryFilter = StationaryPositionFilter()
    private val headingStabilizer = HeadingStabilizer()
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            source.fixes()
                .catch { /* Permission retirée ou GPS coupé : on garde la dernière position connue. */ }
                .collect { _fixes.value = clean(it) }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun clean(raw: GpsFix): GpsFix =
        headingStabilizer.stabilize(stationaryFilter.filter(speedSmoother.smooth(raw)))
}
