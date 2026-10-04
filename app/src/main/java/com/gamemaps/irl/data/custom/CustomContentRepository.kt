package com.gamemaps.irl.data.custom

import android.content.Context
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.vehicle3d.VehicleKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Thèmes et véhicules créés en mode développeur, mémorisés sur le téléphone. */
class CustomContentRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val _themes = MutableStateFlow(CustomContentSerializer.themesFromJson(prefs.getString(KEY_THEMES, null)))
    val themes: StateFlow<List<CustomThemeSpec>> = _themes.asStateFlow()

    private val _vehicles = MutableStateFlow(CustomContentSerializer.vehiclesFromJson(prefs.getString(KEY_VEHICLES, null)))
    val vehicles: StateFlow<List<CustomVehicleSpec>> = _vehicles.asStateFlow()

    /** Ajoute le thème, ou remplace celui qui a le même identifiant. */
    fun saveTheme(theme: CustomThemeSpec) {
        _themes.value = upsert(_themes.value, theme) { it.id }
        prefs.edit().putString(KEY_THEMES, CustomContentSerializer.themesToJson(_themes.value)).apply()
    }

    fun deleteTheme(id: String) {
        _themes.value = _themes.value.filterNot { it.id == id }
        prefs.edit().putString(KEY_THEMES, CustomContentSerializer.themesToJson(_themes.value)).apply()
    }

    /** Ajoute le véhicule, ou remplace celui qui a le même identifiant. */
    fun saveVehicle(vehicle: CustomVehicleSpec) {
        _vehicles.value = upsert(_vehicles.value, vehicle.clamped()) { it.id }
        prefs.edit().putString(KEY_VEHICLES, CustomContentSerializer.vehiclesToJson(_vehicles.value)).apply()
    }

    fun deleteVehicle(id: String) {
        _vehicles.value = _vehicles.value.filterNot { it.id == id }
        prefs.edit().putString(KEY_VEHICLES, CustomContentSerializer.vehiclesToJson(_vehicles.value)).apply()
    }

    /** Thème fourni ou personnalisé portant cet identifiant ; null s'il n'existe plus. */
    fun findTheme(id: String?): MapTheme? =
        MapTheme.entries.firstOrNull { it.id == id } ?: _themes.value.firstOrNull { it.id == id }?.toMapTheme()

    /** Véhicule fourni ou personnalisé portant cet identifiant ; null s'il n'existe plus. */
    fun findVehicle(id: String?): VehicleKind? =
        VehicleKind.entries.firstOrNull { it.id == id } ?: _vehicles.value.firstOrNull { it.id == id }?.toVehicleKind()

    private fun <T> upsert(list: List<T>, item: T, id: (T) -> String): List<T> =
        if (list.any { id(it) == id(item) }) list.map { if (id(it) == id(item)) item else it } else list + item

    private companion object {
        const val FILE_NAME = "custom_content"
        const val KEY_THEMES = "themes"
        const val KEY_VEHICLES = "vehicles"
    }
}
