package com.gamemaps.irl.data.speedlimit

import android.util.Log
import com.gamemaps.irl.core.geo.BoundingBox
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Limitation de vitesse de la route actuelle (km/h), null si inconnue.
 *
 * Charge les routes d'une zone de ~1 km autour du véhicule, puis cherche localement à chaque
 * position GPS. Recharge une nouvelle zone quand on approche du bord de la précédente.
 */
class SpeedLimitRepository(
    private val scope: CoroutineScope,
    private val location: LocationRepository,
    private val overpass: OverpassClient,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _limitKmh = MutableStateFlow<Int?>(null)
    val limitKmh: StateFlow<Int?> = _limitKmh.asStateFlow()

    private var index: SpeedLimitIndex? = null
    private var loadedArea: BoundingBox? = null
    private var loadJob: Job? = null
    private var lastFailureMillis: Long? = null

    fun start() {
        scope.launch {
            location.fixes.filterNotNull().collect(::onFix)
        }
    }

    private fun onFix(fix: GpsFix) {
        _limitKmh.value = index?.limitAt(fix.position, fix.bearingDegrees)
        if (needsNewArea(fix)) loadArea(fix)
    }

    private fun needsNewArea(fix: GpsFix): Boolean {
        if (loadJob?.isActive == true) return false
        val failure = lastFailureMillis
        if (failure != null && clock() - failure < RETRY_DELAY_MS) return false
        val area = loadedArea ?: return true
        return !area.shrink(REFRESH_MARGIN_METERS).contains(fix.position)
    }

    private fun loadArea(fix: GpsFix) {
        val area = BoundingBox.around(fix.position, AREA_RADIUS_METERS)
        loadJob = scope.launch {
            try {
                val roads = overpass.roadsWithMaxSpeed(area)
                Log.i(TAG, "Zone chargée : ${roads.size} routes avec limitation")
                val newIndex = SpeedLimitIndex(roads)
                index = newIndex
                loadedArea = area
                lastFailureMillis = null
                _limitKmh.value = newIndex.limitAt(fix.position, fix.bearingDegrees)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Serveur saturé ou réseau absent : on réessaiera plus tard sans bloquer le reste.
                Log.w(TAG, "Chargement des limitations impossible : ${e.message}")
                lastFailureMillis = clock()
            }
        }
    }

    private companion object {
        const val TAG = "SpeedLimit"
        const val AREA_RADIUS_METERS = 600.0
        const val REFRESH_MARGIN_METERS = 150.0
        const val RETRY_DELAY_MS = 30_000L
    }
}
