package com.gamemaps.irl.ui.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.maps.MapView

/** Relaie le cycle de vie de l'écran vers la MapView (obligatoire pour MapLibre). */
class MapViewLifecycleObserver(private val mapView: MapView) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            else -> Unit // ON_CREATE et ON_DESTROY sont gérés par MapViewHost.
        }
    }
}
