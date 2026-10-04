package com.gamemaps.irl.data.settings

import android.content.Context
import com.gamemaps.irl.map.theme.MapTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Réglages mémorisés sur le téléphone, partagés avec Android Auto. */
class SettingsRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun update(change: (AppSettings) -> AppSettings) {
        val newValue = change(_settings.value)
        prefs.edit()
            .putString(KEY_THEME, newValue.theme.name)
            .putString(KEY_PERSPECTIVE, newValue.perspective.name)
            .putBoolean(KEY_VOICE, newValue.voiceGuidance)
            .putBoolean(KEY_RADAR_BEEPS, newValue.radarBeeps)
            .putBoolean(KEY_SPEEDING_BEEP, newValue.speedingBeep)
            .putBoolean(KEY_RADAR_ALERTS, newValue.radarAlerts)
            .apply()
        _settings.value = newValue
    }

    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            theme = enumOrDefault(prefs.getString(KEY_THEME, null), defaults.theme),
            perspective = enumOrDefault(prefs.getString(KEY_PERSPECTIVE, null), defaults.perspective),
            voiceGuidance = prefs.getBoolean(KEY_VOICE, defaults.voiceGuidance),
            radarBeeps = prefs.getBoolean(KEY_RADAR_BEEPS, defaults.radarBeeps),
            speedingBeep = prefs.getBoolean(KEY_SPEEDING_BEEP, defaults.speedingBeep),
            radarAlerts = prefs.getBoolean(KEY_RADAR_ALERTS, defaults.radarAlerts),
        )
    }

    /** Une valeur inconnue (thème supprimé dans une version future…) retombe sur la valeur par défaut. */
    private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default

    private companion object {
        const val FILE_NAME = "settings"
        const val KEY_THEME = "theme"
        const val KEY_PERSPECTIVE = "perspective"
        const val KEY_VOICE = "voice_guidance"
        const val KEY_RADAR_BEEPS = "radar_beeps"
        const val KEY_SPEEDING_BEEP = "speeding_beep"
        const val KEY_RADAR_ALERTS = "radar_alerts"
    }
}
