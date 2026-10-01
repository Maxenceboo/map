package com.gamemaps.irl

import android.app.Application
import com.gamemaps.irl.di.AppContainer
import org.maplibre.android.MapLibre

/** Point d'entrée du processus : initialise MapLibre et crée le conteneur de dépendances. */
class GameMapsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Doit être appelé avant la création de toute MapView (téléphone ou voiture).
        MapLibre.getInstance(this)
        container = AppContainer(this)
    }
}
