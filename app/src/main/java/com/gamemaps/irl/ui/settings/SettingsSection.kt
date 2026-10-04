package com.gamemaps.irl.ui.settings

/**
 * Écrans du menu Paramètres, en arborescence (règle §2.3) :
 * Paramètres > Thème, Perspective, Véhicule (> Modèle, Couleur), Audio, Radars, Trafic, Lieux (> Maison, Travail, Nouveau favori), Lieux, À propos.
 *
 * @property parent écran vers lequel "← Retour" ramène ; null pour la racine (retour = fermer le menu).
 */
enum class SettingsSection(val title: String, val parent: SettingsSection?) {
    ROOT("Paramètres", null),
    THEME("Thème de la carte", ROOT),
    PERSPECTIVE("Perspective", ROOT),
    VEHICLE("Véhicule", ROOT),
    VEHICLE_MODEL("Modèle de véhicule", VEHICLE),
    VEHICLE_COLOR("Couleur de carrosserie", VEHICLE),
    AUDIO("Audio", ROOT),
    RADARS("Radars", ROOT),
    TRAFFIC("Trafic", ROOT),
    PLACES("Lieux enregistrés", ROOT),
    PLACE_HOME("Maison", PLACES),
    PLACE_WORK("Travail", PLACES),
    PLACE_FAVORITE("Nouveau favori", PLACES),
    ABOUT("À propos", ROOT),

    // Mode développeur : création de thèmes et de véhicules.
    DEV_THEMES("Mes thèmes", ROOT),
    DEV_THEME_EDIT("Éditer le thème", DEV_THEMES),
    DEV_VEHICLES("Mes véhicules", ROOT),
    DEV_VEHICLE_EDIT("Éditer le véhicule", DEV_VEHICLES),
}
