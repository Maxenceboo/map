package com.gamemaps.irl.car

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.Session
import com.gamemaps.irl.car.intent.NavigationRequest
import com.gamemaps.irl.car.intent.NavigationRequestParser
import com.gamemaps.irl.car.screens.CarSearchScreen
import com.gamemaps.irl.car.screens.NavigationCarScreen
import com.gamemaps.irl.data.location.LocationPermissions
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.di.appContainer

/**
 * Une connexion à la voiture. Crée le premier écran, démarre le GPS si autorisé,
 * et répond aux demandes de l'assistant ("Ok Google, emmène-moi à la gare").
 */
class GameMapsCarSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        val container = carContext.appContainer
        if (LocationPermissions.isGranted(carContext)) container.locationRepository.start()

        val mapScreen = NavigationCarScreen(carContext, container)
        val searchScreen = handle(intent) ?: return mapScreen
        // L'app a été ouverte par une demande à chercher : la recherche s'affiche par-dessus la carte.
        screenManager.push(mapScreen)
        return searchScreen
    }

    /** L'app est déjà ouverte dans la voiture quand la demande arrive. */
    override fun onNewIntent(intent: Intent) {
        val searchScreen = handle(intent) ?: return
        screenManager.popToRoot()
        screenManager.push(searchScreen)
    }

    /**
     * Un point précis démarre le guidage tout de suite ; un texte renvoie l'écran de recherche à afficher.
     * Les autres intentions (simple ouverture de l'app) ne font rien.
     */
    private fun handle(intent: Intent): Screen? {
        if (intent.action != CarContext.ACTION_NAVIGATE) return null
        val container = carContext.appContainer
        return when (val request = NavigationRequestParser.parse(intent.dataString)) {
            is NavigationRequest.ToPosition -> {
                val place = Place(id = "car-request", name = request.label ?: "Destination", subtitle = "", position = request.position)
                container.navigationEngine.start(place, autoStart = true)
                null
            }
            is NavigationRequest.ToQuery -> CarSearchScreen(carContext, container, initialQuery = request.query)
            null -> null
        }
    }

    private val screenManager: ScreenManager get() = carContext.getCarService(ScreenManager::class.java)
}
