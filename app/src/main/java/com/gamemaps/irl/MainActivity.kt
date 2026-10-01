package com.gamemaps.irl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamemaps.irl.di.appContainer
import com.gamemaps.irl.ui.screen.MainScreen
import com.gamemaps.irl.ui.screen.MainViewModel
import com.gamemaps.irl.ui.theme.GameMapsTheme

/** Écran unique du téléphone. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = appContainer
        setContent {
            GameMapsTheme {
                MainScreen(
                    viewModel = viewModel(factory = MainViewModel.factory(container)),
                    onLocationPermissionGranted = container.locationRepository::start,
                )
            }
        }
    }
}
