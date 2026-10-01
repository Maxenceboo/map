package com.gamemaps.irl.car.surface

import android.app.Presentation
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.map.MapSetup
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.theme.MapTheme
import org.maplibre.android.maps.MapView

/**
 * Dessine la carte MapLibre sur l'écran de la voiture.
 *
 * Android Auto nous prête une `Surface` (zone derrière les templates). On crée un écran
 * virtuel privé qui dessine dans cette Surface, et on y affiche une [Presentation]
 * contenant une MapView normale. C'est la méthode standard des apps de navigation natives,
 * sans permission spéciale (contrairement à l'option C du cahier des charges).
 */
class CarMapSurface(
    private val carContext: CarContext,
    private val theme: MapTheme,
    private val onControllerChanged: (MapController?) -> Unit,
) : SurfaceCallback {

    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null
    private var mapView: MapView? = null

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        val surface = surfaceContainer.surface ?: return
        release()

        val display = carContext.getSystemService(DisplayManager::class.java).createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            surfaceContainer.width,
            surfaceContainer.height,
            surfaceContainer.dpi,
            surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
        )
        val presentation = Presentation(carContext, display.display)
        val mapView = MapView(presentation.context)
        presentation.setContentView(mapView)

        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        presentation.show()

        mapView.getMapAsync { map ->
            MapSetup.load(
                map = map,
                theme = theme,
                cameraConfig = CameraConfig.CAR,
                viewHeightPx = { mapView.height },
                interactive = false,
                onReady = onControllerChanged,
            )
        }

        this.virtualDisplay = display
        this.presentation = presentation
        this.mapView = mapView
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        // Zone non masquée par les templates : servira à décaler la caméra plus tard.
    }

    override fun onStableAreaChanged(stableArea: Rect) = Unit

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        release()
    }

    fun release() {
        onControllerChanged(null)
        mapView?.apply {
            onPause()
            onStop()
            onDestroy()
        }
        presentation?.dismiss()
        virtualDisplay?.release()
        mapView = null
        presentation = null
        virtualDisplay = null
    }

    private companion object {
        const val VIRTUAL_DISPLAY_NAME = "GameMapsCarMap"
    }
}
