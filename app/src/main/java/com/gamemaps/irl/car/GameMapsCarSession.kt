package com.gamemaps.irl.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.gamemaps.irl.car.screens.NavigationCarScreen
import com.gamemaps.irl.data.location.LocationPermissions
import com.gamemaps.irl.di.appContainer

/** Une connexion à la voiture. Crée le premier écran et démarre le GPS si autorisé. */
class GameMapsCarSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        val container = carContext.appContainer
        if (LocationPermissions.isGranted(carContext)) container.locationRepository.start()
        return NavigationCarScreen(carContext, container)
    }
}
