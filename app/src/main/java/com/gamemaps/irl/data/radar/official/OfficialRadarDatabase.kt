package com.gamemaps.irl.data.radar.official

import android.content.res.AssetManager
import com.gamemaps.irl.core.geo.BoundingBox
import com.gamemaps.irl.data.radar.Radar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Base officielle des ~3 350 radars français, embarquée dans l'application (fonctionne hors ligne).
 * Chargée une seule fois, à la première demande, puis gardée en mémoire (~quelques centaines de Ko).
 */
class OfficialRadarDatabase(private val assets: AssetManager) {

    private val mutex = Mutex()
    private var radars: List<Radar>? = null

    suspend fun inArea(box: BoundingBox): List<Radar> = all().filter { box.contains(it.position) }

    private suspend fun all(): List<Radar> = mutex.withLock {
        radars ?: withContext(Dispatchers.IO) {
            OfficialRadarParser.parse(assets.open(ASSET_NAME).bufferedReader().use { it.readText() })
        }.also { radars = it }
    }

    private companion object {
        /** Généré par `tools/convert_radars.py`. */
        const val ASSET_NAME = "radars_france.json"
    }
}
