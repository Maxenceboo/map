package com.gamemaps.irl.core.geo

/**
 * Coordonnée géographique WGS84, en degrés décimaux.
 *
 * Type maison volontairement indépendant de MapLibre : toute la logique métier
 * (routing, navigation, recherche) l'utilise, et seule la couche `map` convertit
 * vers le type MapLibre.
 */
data class LatLng(val lat: Double, val lng: Double)
