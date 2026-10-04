package com.gamemaps.irl.ui.settings.sections

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gamemaps.irl.ui.icons.HudIcons
import com.gamemaps.irl.ui.settings.components.SettingsGroup
import com.gamemaps.irl.ui.settings.components.SettingsInfoRow
import com.gamemaps.irl.ui.settings.components.SettingsPlainRow
import com.gamemaps.irl.ui.settings.components.SettingsToggleRow

/** Nombre d'appuis sur la version pour activer le mode développeur (comme dans les réglages d'Android). */
private const val TAPS_TO_UNLOCK = 7

/**
 * Version et sources des données (les licences ouvertes demandent de les citer).
 * Sept appuis sur la version activent le mode développeur ; un interrupteur permet ensuite de le couper.
 */
@Composable
fun AboutSection(devMode: Boolean, onSetDevMode: (Boolean) -> Unit) {
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"
    }
    var taps by remember { mutableIntStateOf(0) }
    // Un seul message à la fois : le précédent est retiré, sinon ils s'empilent et s'affichent en retard.
    val toast = remember { arrayOfNulls<Toast>(1) }
    val show = { text: String ->
        toast[0]?.cancel()
        toast[0] = Toast.makeText(context, text, Toast.LENGTH_SHORT).also { it.show() }
    }

    SettingsGroup {
        SettingsPlainRow(Icons.Filled.Info, "Game Maps IRL", "Version $version, natif Kotlin, Android Auto") {
            if (devMode) return@SettingsPlainRow
            taps++
            val remaining = TAPS_TO_UNLOCK - taps
            when {
                remaining <= 0 -> {
                    onSetDevMode(true)
                    show("Mode développeur activé")
                }
                remaining <= 3 -> show("Encore $remaining appui(s) pour le mode développeur")
            }
        }
    }

    if (devMode) {
        SettingsGroup("Développeur") {
            SettingsToggleRow(Icons.Filled.Build, "Mode développeur", "Création de thèmes et de véhicules dans le menu Paramètres", true) { enabled ->
                taps = 0
                onSetDevMode(enabled)
            }
        }
    }

    SettingsGroup("Données") {
        SettingsInfoRow(HudIcons.Map, "Carte", "© OpenStreetMap contributors, tuiles OpenFreeMap")
        SettingsInfoRow(Icons.Filled.Home, "Adresses", "Base Adresse Nationale (IGN Géoplateforme)")
        SettingsInfoRow(Icons.Filled.Place, "Lieux", "Photon (Komoot), données OpenStreetMap")
        SettingsInfoRow(HudIcons.Navigation, "Itinéraires", "TomTom (avec trafic) ou OSRM, données OpenStreetMap")
        SettingsInfoRow(HudIcons.Radar, "Radars", "Base officielle française + OpenStreetMap (Overpass)")
    }
}
