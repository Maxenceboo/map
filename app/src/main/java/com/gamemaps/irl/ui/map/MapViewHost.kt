package com.gamemaps.irl.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.map.MapSetup
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.theme.MapTheme
import org.maplibre.android.maps.MapView

/** Intègre une MapView MapLibre dans Compose et renvoie un [MapController] une fois la carte prête. */
@Composable
fun MapViewHost(
    theme: MapTheme,
    modifier: Modifier = Modifier,
    onControllerReady: (MapController) -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnReady = rememberUpdatedState(onControllerReady)

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                MapSetup.load(
                    map = map,
                    theme = theme,
                    cameraConfig = CameraConfig.PHONE,
                    viewHeightPx = { height },
                    interactive = true,
                    onReady = { currentOnReady.value(it) },
                )
            }
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = MapViewLifecycleObserver(mapView)
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
