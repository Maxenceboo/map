package com.gamemaps.irl.data.search

import com.gamemaps.irl.core.geo.LatLng

/**
 * Un lieu trouvé par la recherche (adresse, rue, ville, point d'intérêt) ou enregistré en favori.
 *
 * @property name libellé principal ("Gare Saint-Jean", "12 Rue Sainte-Catherine").
 * @property subtitle complément ("33800 Bordeaux").
 * @property category pour un point d'intérêt, sa catégorie OpenStreetMap ("railway:station", "amenity:fuel").
 */
data class Place(
    val id: String,
    val name: String,
    val subtitle: String,
    val position: LatLng,
    val kind: PlaceKind = PlaceKind.ADDRESS,
    val category: String? = null,
)
