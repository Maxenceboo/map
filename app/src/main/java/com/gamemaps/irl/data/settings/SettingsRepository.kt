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
            .putString(KEY_VEHICLE, newValue.vehicle.name)
            .putString(KEY_VEHICLE_COLOR, newValue.vehicleColor.name)
            .putBoolean(KEY_HEADLIGHTS, newValue.headlights)
            .putBoolean(KEY_VOICE, newValue.voiceGuidance)
            .putBoolean(KEY_RADAR_BEEPS, newValue.radarBeeps)
            .putBoolean(KEY_SPEEDING_BEEP, newValue.speedingBeep)
            .putBoolean(KEY_RADAR_ALERTS, newValue.radarAlerts)
            .putBoolean(KEY_TRAFFIC, newValue.traffic)
            .apply()
        _settings.value = newValue
    }

    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            theme = enumOrDefault(prefs.getString(KEY_THEME, null), defaults.theme),
            perspective = enumOrDefault(prefs.getString(KEY_PERSPECTIVE, null), defaults.perspective),
            vehicle = enumOrDefault(prefs.getString(KEY_VEHICLE, null), defaults.vehicle),
            vehicleColor = enumOrDefault(prefs.getString(KEY_VEHICLE_COLOR, null), defaults.vehicleColor),
            headlights = prefs.getBoolean(KEY_HEADLIGHTS, defaults.headlights),
            voiceGuidance = prefs.getBoolean(KEY_VOICE, defaults.voiceGuidance),
            radarBeeps = prefs.getBoolean(KEY_RADAR_BEEPS, defaults.radarBeeps),
            speedingBeep = prefs.getBoolean(KEY_SPEEDING_BEEP, defaults.speedingBeep),
            radarAlerts = prefs.getBoolean(KEY_RADAR_ALERTS, defaults.radarAlerts),
            traffic = prefs.getBoolean(KEY_TRAFFIC, defaults.traffic),
        )
    }

    /** Une valeur inconnue (thème supprimé dans une version future…) retombe sur la valeur par défaut. */
    private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default

    private companion object {
        const val FILE_NAME = "settings"
        const val KEY_THEME = "theme"
        const val KEY_PERSPECTIVE = "perspective"
        const val KEY_VEHICLE = "vehicle"
        const val KEY_VEHICLE_COLOR = "vehicle_color"
        const val KEY_HEADLIGHTS = "headlights"
        const val KEY_VOICE = "voice_guidance"
        const val KEY_RADAR_BEEPS = "radar_beeps"
        const val KEY_SPEEDING_BEEP = "speeding_beep"
        const val KEY_RADAR_ALERTS = "radar_alerts"
        const val KEY_TRAFFIC = "traffic"
    }
}
