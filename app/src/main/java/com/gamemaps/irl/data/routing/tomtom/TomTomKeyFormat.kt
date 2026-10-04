package com.gamemaps.irl.data.routing.tomtom

/** Nettoie et contrôle une clé d'API TomTom saisie ou collée par l'utilisateur. */
object TomTomKeyFormat {

    private const val MIN_LENGTH = 16
    private const val MAX_LENGTH = 64

    /** La clé sans espaces ni retours à la ligne, ou null si elle ne ressemble pas à une clé TomTom. */
    fun normalize(raw: String): String? {
        val key = raw.filterNot { it.isWhitespace() }
        val looksValid = key.length in MIN_LENGTH..MAX_LENGTH && key.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }
        return key.takeIf { looksValid }
    }

    /** Pour l'affichage : seuls les 4 derniers caractères restent lisibles. */
    fun mask(key: String): String = "••••••••" + key.takeLast(4)
}
