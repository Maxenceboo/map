package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pose un véhicule sur la carte : passe du repère du véhicule (mètres, +Z = avant, +X = droite)
 * aux coordonnées GPS, en tenant compte de la position, du cap et de l'échelle. Calcul pur, testable.
 */
object VehicleGeometry {

    /** Chaque pièce du [model], placée autour de [center], tournée selon [bearingDegrees], agrandie de [scale]. */
    fun place(model: VehicleModel, center: LatLng, bearingDegrees: Double, scale: Double, color: VehicleColor): List<PlacedPart> =
        model.parts.map { part ->
            PlacedPart(
                ring = part.footprint.map { toLatLng(it, center, bearingDegrees, scale) },
                baseMeters = part.baseMeters * scale,
                topMeters = part.topMeters * scale,
                colorHex = VehiclePalette.hexFor(part.role, color),
            )
        }

    /**
     * Faisceaux des phares projetés au sol (cahier des charges §3.4, "nappe d'impact lumineuse") :
     * un trapèze par optique, étroit au départ et large [BEAM_LENGTH_METERS] plus loin.
     */
    fun headlightBeams(model: VehicleModel, center: LatLng, bearingDegrees: Double, scale: Double): List<List<LatLng>> {
        val origins = if (model.headlightX == 0.0) listOf(0.0) else listOf(-model.headlightX, model.headlightX)
        return origins.map { x ->
            listOf(
                GroundPoint(x - BEAM_NEAR_HALF_WIDTH, model.frontZ),
                GroundPoint(x + BEAM_NEAR_HALF_WIDTH, model.frontZ),
                GroundPoint(x + BEAM_FAR_HALF_WIDTH, model.frontZ + BEAM_LENGTH_METERS),
                GroundPoint(x - BEAM_FAR_HALF_WIDTH, model.frontZ + BEAM_LENGTH_METERS),
            ).map { toLatLng(it, center, bearingDegrees, scale) }
        }
    }

    /**
     * Le cap est mesuré dans le sens horaire depuis le nord :
     * l'avant (+Z) pointe vers (sin cap, cos cap) en (est, nord), la droite (+X) vers (cos cap, -sin cap).
     */
    fun toLatLng(point: GroundPoint, center: LatLng, bearingDegrees: Double, scale: Double): LatLng {
        val bearing = Math.toRadians(bearingDegrees)
        val x = point.x * scale
        val z = point.z * scale
        val east = x * cos(bearing) + z * sin(bearing)
        val north = -x * sin(bearing) + z * cos(bearing)
        val latitude = center.lat + Math.toDegrees(north / GeoMath.EARTH_RADIUS_METERS)
        val longitude = center.lng + Math.toDegrees(east / (GeoMath.EARTH_RADIUS_METERS * cos(Math.toRadians(center.lat))))
        return LatLng(latitude, longitude)
    }

    private const val BEAM_LENGTH_METERS = 11.0
    private const val BEAM_NEAR_HALF_WIDTH = 0.25
    private const val BEAM_FAR_HALF_WIDTH = 1.9
}
