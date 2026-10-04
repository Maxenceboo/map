package com.gamemaps.irl.data.settings

import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.vehicle3d.VehicleColor
import com.gamemaps.irl.map.vehicle3d.VehicleKind

/** Réglages de l'application, modifiables dans le menu Paramètres. */
data class AppSettings(
    val theme: MapTheme = MapTheme.GTA_RADAR,
    val perspective: Perspective = Perspective.COCKPIT_3D,
    /** Véhicule affiché sur la carte : modèle 3D ou flèche plate. */
    val vehicle: VehicleKind = VehicleKind.SPORT,
    val vehicleColor: VehicleColor = VehicleColor.YELLOW,
    /** Faisceaux des phares projetés sur la route devant le véhicule 3D. */
    val headlights: Boolean = true,
    /** Phrases de guidage ("Tournez à droite…"). Les carillons restent actifs. */
    val voiceGuidance: Boolean = true,
    /** Bips à l'approche d'un radar. */
    val radarBeeps: Boolean = true,
    /** Bip d'excès de vitesse. */
    val speedingBeep: Boolean = true,
    /** Alertes radar (bandeau, voiture, sons). Les radars restent visibles sur la carte. */
    val radarAlerts: Boolean = true,
)
