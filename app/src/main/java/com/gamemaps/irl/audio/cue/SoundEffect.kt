package com.gamemaps.irl.audio.cue

/** Sons de l'application. Leur composition (notes, durées) est dans `synth/SoundEffectTones.kt`. */
enum class SoundEffect {
    /** Deux notes montantes : le guidage démarre. */
    START,

    /** Carillon juste avant une manœuvre. */
    TURN,

    /** Arpège do-mi-sol-do : destination atteinte. */
    ARRIVAL,

    /** Double bip : radar en approche. */
    RADAR_WARNING,

    /** Triple bip plus agressif : radar à moins de 300 m. */
    RADAR_URGENT,

    /** Bip discret : excès de vitesse. */
    SPEEDING,
}
