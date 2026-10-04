package com.gamemaps.irl.di

import android.content.Context
import com.gamemaps.irl.audio.AudioController
import com.gamemaps.irl.audio.playback.AndroidAudioOutput
import com.gamemaps.irl.audio.playback.NavigationAudioFocus
import com.gamemaps.irl.audio.playback.TonePlayer
import com.gamemaps.irl.audio.playback.VoiceGuide
import com.gamemaps.irl.data.location.AndroidLocationSource
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.network.HttpClientFactory
import com.gamemaps.irl.data.osm.OverpassClient
import com.gamemaps.irl.data.radar.RadarRepository
import com.gamemaps.irl.data.routing.RoutingService
import com.gamemaps.irl.data.routing.osrm.OsrmClient
import com.gamemaps.irl.data.search.BanGeocoder
import com.gamemaps.irl.data.search.PlaceSearch
import com.gamemaps.irl.data.settings.AudioPreferences
import com.gamemaps.irl.data.speedlimit.SpeedLimitRepository
import com.gamemaps.irl.navigation.NavigationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Injection de dépendances "à la main" : tous les objets partagés de l'app, créés une seule fois.
 *
 * Le téléphone (MainActivity) et la voiture (Android Auto) lisent le même conteneur,
 * donc partagent la même position et le même guidage.
 */
class AppContainer(context: Context) {

    /** Vit aussi longtemps que le processus. Main : MapLibre et les flows UI s'y attendent. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val httpClient = HttpClientFactory.create()

    private val overpassClient = OverpassClient(httpClient)

    val placeSearch: PlaceSearch = BanGeocoder(httpClient)

    val routingService: RoutingService = OsrmClient(httpClient)

    val locationRepository = LocationRepository(AndroidLocationSource(context.applicationContext), appScope)

    val navigationEngine = NavigationEngine(appScope, locationRepository, routingService)

    // Démarrés tout de suite : ils attendent simplement les premières positions GPS.
    val speedLimitRepository = SpeedLimitRepository(appScope, locationRepository, overpassClient).apply { start() }

    val radarRepository = RadarRepository(appScope, locationRepository, overpassClient).apply { start() }

    val audioPreferences = AudioPreferences(context)

    init {
        val focus = NavigationAudioFocus(context)
        AudioController(
            scope = appScope,
            navigation = navigationEngine.state,
            radarAlerts = radarRepository.alert,
            fixes = locationRepository.fixes,
            speedLimits = speedLimitRepository.limitKmh,
            muted = audioPreferences.muted,
            output = AndroidAudioOutput(TonePlayer(focus), VoiceGuide(context, focus)),
        ).start()
    }
}
