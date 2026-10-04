package com.gamemaps.irl.data.radar

import android.util.Log
import com.gamemaps.irl.core.geo.BoundingBox
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.osm.OverpassClient
import com.gamemaps.irl.data.osm.OverpassQueries
import com.gamemaps.irl.data.radar.official.OfficialRadarDatabase
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
 * Pour chaque zone de ~10 km de côté :
 * 1. la base officielle embarquée (hors ligne, toujours disponible) ;
 * 2. complétée par OpenStreetMap (radars récents, étranger) quand le réseau le permet.
 */
class RadarRepository(
    private val scope: CoroutineScope,
    private val location: LocationRepository,
    private val official: OfficialRadarDatabase,
    private val overpass: OverpassClient,
    private val detector: RadarAlertDetector = RadarAlertDetector(),
) {
    private val _radars = MutableStateFlow<List<Radar>>(emptyList())
    val radars: StateFlow<List<Radar>> = _radars.asStateFlow()

    private val _alert = MutableStateFlow<RadarAlert?>(null)
    val alert: StateFlow<RadarAlert?> = _alert.asStateFlow()

    private var loadedArea: BoundingBox? = null
    private var loadJob: Job? = null

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
        val area = loadedArea ?: return true
        return !area.shrink(REFRESH_MARGIN_METERS).contains(fix.position)
    }

    private fun loadArea(fix: GpsFix) {
        val area = BoundingBox.around(fix.position, AREA_RADIUS_METERS)
        loadJob = scope.launch {
            val officialRadars = official.inArea(area)
            publish(officialRadars, fix) // Affichage immédiat, sans attendre le réseau.
            val osmRadars = loadOsm(area)
            val merged = RadarMerger.merge(officialRadars, osmRadars)
            Log.i(TAG, "Zone chargée : ${officialRadars.size} radars officiels + ${merged.size - officialRadars.size} OSM")
            publish(merged, fix)
            loadedArea = area
        }
    }

    /** OSM est un complément : en cas d'échec, on garde simplement la base officielle. */
    private suspend fun loadOsm(area: BoundingBox): List<Radar> = try {
        RadarResponseParser.parse(overpass.run(OverpassQueries.speedCameras(area)))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Radars OSM indisponibles : ${e.message}")
        emptyList()
    }

    private fun publish(radars: List<Radar>, fix: GpsFix) {
        _radars.value = radars
        _alert.value = detector.detect(fix, radars)
    }

    private companion object {
        const val TAG = "Radar"
        const val AREA_RADIUS_METERS = 5_000.0
        const val REFRESH_MARGIN_METERS = 1_500.0
    }
}
