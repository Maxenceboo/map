package com.gamemaps.irl.data.settings

import android.content.Context
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.vehicle3d.VehicleKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Réglages mémorisés sur le téléphone, partagés avec Android Auto.
 *
 * Le thème et le véhicule sont enregistrés par leur identifiant ; [findTheme] et [findVehicle]
 * retrouvent l'objet correspondant, qu'il soit fourni avec l'app ou créé par l'utilisateur.
 */
class SettingsRepository(
    context: Context,
    private val findTheme: (String?) -> MapTheme?,
    private val findVehicle: (String?) -> VehicleKind?,
) {

    private val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun update(change: (AppSettings) -> AppSettings) {
        val newValue = change(_settings.value)
        prefs.edit()
            .putString(KEY_THEME, newValue.theme.id)
            .putString(KEY_PERSPECTIVE, newValue.perspective.name)
            .putString(KEY_VEHICLE, newValue.vehicle.id)
            .putString(KEY_VEHICLE_COLOR, newValue.vehicleColor.name)
            .putBoolean(KEY_HEADLIGHTS, newValue.headlights)
            .putBoolean(KEY_VOICE, newValue.voiceGuidance)
            .putBoolean(KEY_RADAR_BEEPS, newValue.radarBeeps)
            .putBoolean(KEY_SPEEDING_BEEP, newValue.speedingBeep)
            .putBoolean(KEY_RADAR_ALERTS, newValue.radarAlerts)
            .putBoolean(KEY_TRAFFIC, newValue.traffic)
            .putBoolean(KEY_DEV_MODE, newValue.devMode)
            .putBoolean(KEY_DEMO_LOCATION, newValue.demoLocation)
            .apply()
        _settings.value = newValue
    }

    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            // Un thème ou un véhicule supprimé depuis retombe sur la valeur par défaut.
            theme = findTheme(prefs.getString(KEY_THEME, null)) ?: defaults.theme,
            perspective = enumOrDefault(prefs.getString(KEY_PERSPECTIVE, null), defaults.perspective),
            vehicle = findVehicle(prefs.getString(KEY_VEHICLE, null)) ?: defaults.vehicle,
            vehicleColor = enumOrDefault(prefs.getString(KEY_VEHICLE_COLOR, null), defaults.vehicleColor),
            headlights = prefs.getBoolean(KEY_HEADLIGHTS, defaults.headlights),
            voiceGuidance = prefs.getBoolean(KEY_VOICE, defaults.voiceGuidance),
            radarBeeps = prefs.getBoolean(KEY_RADAR_BEEPS, defaults.radarBeeps),
            speedingBeep = prefs.getBoolean(KEY_SPEEDING_BEEP, defaults.speedingBeep),
            radarAlerts = prefs.getBoolean(KEY_RADAR_ALERTS, defaults.radarAlerts),
            traffic = prefs.getBoolean(KEY_TRAFFIC, defaults.traffic),
            devMode = prefs.getBoolean(KEY_DEV_MODE, defaults.devMode),
            demoLocation = prefs.getBoolean(KEY_DEMO_LOCATION, defaults.demoLocation),
        )
    }

    /** Une valeur inconnue (option supprimée dans une version future…) retombe sur la valeur par défaut. */
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
        const val KEY_DEV_MODE = "dev_mode"
        const val KEY_DEMO_LOCATION = "demo_location"
    }
}
