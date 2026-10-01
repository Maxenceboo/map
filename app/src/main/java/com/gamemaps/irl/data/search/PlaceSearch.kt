package com.gamemaps.irl.data.search

import com.gamemaps.irl.core.geo.LatLng

/** Recherche de lieux par texte. [near] sert à favoriser les résultats proches de l'utilisateur. */
fun interface PlaceSearch {
    suspend fun search(query: String, near: LatLng?): List<Place>
}
