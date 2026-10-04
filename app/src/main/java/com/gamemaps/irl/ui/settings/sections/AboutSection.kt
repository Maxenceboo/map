package com.gamemaps.irl.ui.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gamemaps.irl.ui.settings.components.SettingsGroupTitle
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow

/** Version et sources des données (les licences ouvertes demandent de les citer). */
@Composable
fun AboutSection() {
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"
    }
    SettingsInfoRow("🎮", "Game Maps IRL", "Version $version — natif Kotlin, Android Auto")

    SettingsGroupTitle("Données")
    SettingsInfoRow("🗺", "Carte", "© OpenStreetMap contributors, tuiles OpenFreeMap")
    SettingsInfoRow("🏠", "Adresses", "Base Adresse Nationale (IGN Géoplateforme)")
    SettingsInfoRow("📍", "Lieux", "Photon (Komoot), données OpenStreetMap")
    SettingsInfoRow("🧭", "Itinéraires", "OSRM, données OpenStreetMap")
    SettingsInfoRow("📸", "Radars", "Base officielle française + OpenStreetMap (Overpass)")
}
