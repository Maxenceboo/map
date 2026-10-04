package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.BuildConfig

/**
 * Clé d'API TomTom. Elle n'est jamais écrite dans le code : Gradle la lit dans `local.properties`
 * (ligne `tomtom.apiKey=...`, fichier non versionné) au moment de la compilation.
 */
object TomTomApiKey {

    /** null si aucune clé n'est installée : l'app utilise alors OSRM, sans trafic. */
    val value: String? = BuildConfig.TOMTOM_API_KEY.trim().takeIf { it.isNotEmpty() }

    val isConfigured: Boolean get() = value != null
}
