package com.gamemaps.irl.data.settings

import com.gamemaps.irl.map.theme.MapTheme

/** Réglages de l'application, modifiables dans le menu Paramètres. */
data class AppSettings(
    val theme: MapTheme = MapTheme.GTA_RADAR,
    val perspective: Perspective = Perspective.COCKPIT_3D,
    /** Phrases de guidage ("Tournez à droite…"). Les carillons restent actifs. */
    val voiceGuidance: Boolean = true,
    /** Bips à l'approche d'un radar. */
    val radarBeeps: Boolean = true,
    /** Bip d'excès de vitesse. */
    val speedingBeep: Boolean = true,
    /** Alertes radar (bandeau, voiture, sons). Les radars restent visibles sur la carte. */
    val radarAlerts: Boolean = true,
)
