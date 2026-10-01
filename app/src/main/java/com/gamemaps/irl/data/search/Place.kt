package com.gamemaps.irl.data.search

import com.gamemaps.irl.core.geo.LatLng

/**
 * Un lieu trouvé par la recherche (adresse, rue, ville...).
 *
 * @property name libellé principal ("12 Rue Sainte-Catherine").
 * @property subtitle complément ("33000 Bordeaux").
 */
data class Place(
    val id: String,
    val name: String,
    val subtitle: String,
    val position: LatLng,
)
