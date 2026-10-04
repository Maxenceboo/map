package com.gamemaps.irl.data.routing.tomtom

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clé d'API TomTom collée par l'utilisateur dans Paramètres > Trafic.
 *
 * Elle reste dans le stockage privé de l'app sur ce téléphone : elle n'est ni dans le code,
 * ni dans l'APK, ni dans les sauvegardes Google (fichier exclu, voir `res/xml/backup_rules.xml`).
 */
class TomTomKeyStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _key = MutableStateFlow(prefs.getString(KEY, null)?.let(TomTomKeyFormat::normalize))

    /** null tant qu'aucune clé n'est enregistrée : l'app calcule alors ses itinéraires sans trafic. */
    val key: StateFlow<String?> = _key.asStateFlow()

    /** Enregistre la clé. Renvoie false (et ne change rien) si elle n'a pas la forme d'une clé TomTom. */
    fun save(raw: String): Boolean {
        val key = TomTomKeyFormat.normalize(raw) ?: return false
        prefs.edit().putString(KEY, key).apply()
        _key.value = key
        return true
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
        _key.value = null
    }

    companion object {
        /** Nom du fichier de préférences : le même que dans les règles de sauvegarde. */
        const val FILE_NAME = "tomtom_key"
        private const val KEY = "api_key"
    }
}
