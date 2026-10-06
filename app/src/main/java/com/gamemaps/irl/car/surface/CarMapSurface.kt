package com.gamemaps.irl.car.surface

import android.app.Presentation
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.view.Gravity
import android.widget.FrameLayout
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import com.gamemaps.irl.data.speedlimit.SpeedLimit
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
 * contenant une MapView normale, avec le compteur de vitesse par-dessus. C'est la méthode standard des apps de navigation natives,
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
    private var speedView: CarSpeedView? = null
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    /** Zone de la carte que les éléments d'Android Auto ne recouvrent pas. */
    private var visibleArea: Rect? = null

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
        // Le compteur de vitesse est posé par-dessus la carte, dans le même écran virtuel.
        val speedView = CarSpeedView(presentation.context)
        val root = FrameLayout(presentation.context).apply {
            addView(mapView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            addView(speedView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        }
        presentation.setContentView(root)
        surfaceWidth = surfaceContainer.width
        surfaceHeight = surfaceContainer.height

        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        presentation.show()

        mapView.getMapAsync { map ->
            MapSetup.load(
                context = presentation.context,
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
        this.speedView = speedView
        placeSpeedView()
    }

    /** Vitesse et limitation à afficher sur l'écran de la voiture. */
    fun showSpeed(speedKmh: Int, limit: SpeedLimit?) {
        speedView?.show(speedKmh, limit)
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        this.visibleArea = Rect(visibleArea)
        placeSpeedView()
    }

    /**
     * Le compteur va en bas à droite de la zone visible : la manœuvre est en haut à gauche,
     * l'estimation d'arrivée en bas à gauche et les boutons en haut à droite.
     */
    private fun placeSpeedView() {
        val view = speedView ?: return
        val area = visibleArea ?: Rect(0, 0, surfaceWidth, surfaceHeight)
        val margin = (MARGIN_DP * view.resources.displayMetrics.density).toInt()
        view.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            rightMargin = (surfaceWidth - area.right).coerceAtLeast(0) + margin
            bottomMargin = (surfaceHeight - area.bottom).coerceAtLeast(0) + margin
        }
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
        speedView = null
        presentation = null
        virtualDisplay = null
    }

    private companion object {
        const val VIRTUAL_DISPLAY_NAME = "GameMapsCarMap"
        const val MARGIN_DP = 16
    }
}
