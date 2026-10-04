package com.gamemaps.irl.di

import android.content.Context
import com.gamemaps.irl.data.location.AndroidLocationSource
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.network.HttpClientFactory
import com.gamemaps.irl.data.routing.RoutingService
import com.gamemaps.irl.data.routing.osrm.OsrmClient
import com.gamemaps.irl.data.search.BanGeocoder
import com.gamemaps.irl.data.search.PlaceSearch
import com.gamemaps.irl.data.speedlimit.OverpassClient
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

    val placeSearch: PlaceSearch = BanGeocoder(httpClient)

    val routingService: RoutingService = OsrmClient(httpClient)

    val locationRepository = LocationRepository(AndroidLocationSource(context.applicationContext), appScope)

    val navigationEngine = NavigationEngine(appScope, locationRepository, routingService)

    /** Démarrée tout de suite : elle attend simplement les premières positions GPS. */
    val speedLimitRepository = SpeedLimitRepository(appScope, locationRepository, OverpassClient(httpClient))
        .apply { start() }
}
