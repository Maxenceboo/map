package com.gamemaps.irl.ui.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow

/** Version et sources des données (les licences ouvertes demandent de les citer). */
@Composable
fun AboutSection() {
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"
    }

    SettingsGroup {
        SettingsInfoRow(Icons.Filled.Info, "Game Maps IRL", "Version $version, natif Kotlin, Android Auto")
    }
    SettingsGroup("Données") {
        SettingsInfoRow(HudIcons.Map, "Carte", "© OpenStreetMap contributors, tuiles OpenFreeMap")
        SettingsInfoRow(Icons.Filled.Home, "Adresses", "Base Adresse Nationale (IGN Géoplateforme)")
        SettingsInfoRow(Icons.Filled.Place, "Lieux", "Photon (Komoot), données OpenStreetMap")
        SettingsInfoRow(HudIcons.Navigation, "Itinéraires", "TomTom (avec trafic) ou OSRM, données OpenStreetMap")
        SettingsInfoRow(HudIcons.Radar, "Radars", "Base officielle française + OpenStreetMap (Overpass)")
    }
}
