package com.gamemaps.irl.data.radar

import android.util.Log
import com.gamemaps.irl.core.geo.BoundingBox
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.osm.OverpassClient
import com.gamemaps.irl.data.osm.OverpassQueries
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.navigation.radar.RadarAlertDetector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Radars autour du véhicule et alerte en cours.
 *
 * Charge les radars d'une zone de ~10 km de côté (peu de données : quelques dizaines de points),
 * et en recharge une nouvelle quand on approche du bord.
 */
class RadarRepository(
    private val scope: CoroutineScope,
    private val location: LocationRepository,
    private val overpass: OverpassClient,
    private val detector: RadarAlertDetector = RadarAlertDetector(),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _radars = MutableStateFlow<List<Radar>>(emptyList())
    val radars: StateFlow<List<Radar>> = _radars.asStateFlow()

    private val _alert = MutableStateFlow<RadarAlert?>(null)
    val alert: StateFlow<RadarAlert?> = _alert.asStateFlow()

    private var loadedArea: BoundingBox? = null
    private var loadJob: Job? = null
    private var lastFailureMillis: Long? = null

    fun start() {
        scope.launch {
            location.fixes.filterNotNull().collect(::onFix)
        }
    }

    private fun onFix(fix: GpsFix) {
        _alert.value = detector.detect(fix, _radars.value)
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
                val radars = RadarResponseParser.parse(overpass.run(OverpassQueries.speedCameras(area)))
                Log.i(TAG, "Zone chargée : ${radars.size} radars")
                _radars.value = radars
                loadedArea = area
                lastFailureMillis = null
                _alert.value = detector.detect(fix, radars)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Chargement des radars impossible : ${e.message}")
                lastFailureMillis = clock()
            }
        }
    }

    private companion object {
        const val TAG = "Radar"
        const val AREA_RADIUS_METERS = 5_000.0
        const val REFRESH_MARGIN_METERS = 1_500.0
        const val RETRY_DELAY_MS = 30_000L
    }
}
