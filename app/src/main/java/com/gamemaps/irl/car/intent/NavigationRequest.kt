package com.gamemaps.irl.car.intent

import com.gamemaps.irl.core.geo.LatLng

/** Ce que demande l'assistant de la voiture ("Ok Google, emmène-moi à…"). */
sealed interface NavigationRequest {

    /** Un point précis : le guidage peut démarrer tout de suite. */
    data class ToPosition(val position: LatLng, val label: String?) : NavigationRequest

    /** Un texte ("gare d'Arcachon") : il faut d'abord chercher le lieu. */
    data class ToQuery(val query: String) : NavigationRequest
}
