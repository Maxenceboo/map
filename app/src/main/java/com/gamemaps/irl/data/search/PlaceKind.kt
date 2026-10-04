package com.gamemaps.irl.data.search

/** Nature d'un résultat de recherche (sert à l'icône et au classement). */
enum class PlaceKind {
    /** Adresse précise avec numéro. */
    ADDRESS,

    /** Rue, avenue, route. */
    STREET,

    /** Ville, village, lieu-dit. */
    CITY,

    /** Point d'intérêt : gare, station-service, magasin, restaurant… */
    POI,
}
