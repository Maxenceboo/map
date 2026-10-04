package com.gamemaps.irl.ui.settings

/** Écrans du menu Paramètres. Arborescence : Paramètres > Thème, Perspective, Audio, Radars, Lieux, À propos. */
enum class SettingsSection(val title: String) {
    ROOT("Paramètres"),
    THEME("Thème de la carte"),
    PERSPECTIVE("Perspective"),
    AUDIO("Audio"),
    RADARS("Radars"),
    PLACES("Lieux enregistrés"),
    ABOUT("À propos"),
}
