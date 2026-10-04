package com.gamemaps.irl.map.vehicle3d

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Un morceau de lumière des phares, dans le repère du véhicule (mètres).
 * [baseMeters] = [topMeters] = 0 pour une nappe posée au sol ; sinon c'est un volume.
 */
data class BeamShape(
    val footprint: List<GroundPoint>,
    val baseMeters: Double,
    val topMeters: Double,
    val opacity: Double,
)

/**
 * Lumière des phares (cahier des charges §3.4), en deux couches par optique :
 * - une lueur au sol : trois gouttes arrondies emboîtées, de plus en plus lumineuses vers le centre ;
 * - un volume translucide : trois tranches qui partent de la hauteur du phare et descendent
 *   vers la route, pour donner l'épaisseur d'un faisceau.
 */
object HeadlightBeams {

    private const val NEAR_HALF_WIDTH = 0.22
    private const val ARC_STEPS = 12

    /** Longueur, demi-largeur au bout, opacité : du halo extérieur au cœur du faisceau. */
    private val GLOW_LAYERS = listOf(
        Triple(13.0, 2.6, 0.08),
        Triple(10.5, 1.9, 0.10),
        Triple(8.0, 1.2, 0.14),
    )

    private const val VOLUME_LENGTH = 7.0
    private const val VOLUME_HALF_WIDTH = 1.0

    /** Début et fin de chaque tranche le long du faisceau, puis hauteurs basse et haute. */
    private val VOLUME_SLICES = listOf(
        listOf(0.0, 2.2, 0.30, 0.80),
        listOf(2.2, 4.4, 0.12, 0.60),
    )
    private const val LAST_SLICE_START = 4.4
    private const val LAST_SLICE_TOP = 0.38

    /** Position latérale de chaque optique : une seule au centre pour une moto. */
    private fun origins(model: VehicleModel): List<Double> =
        if (model.headlightX == 0.0) listOf(0.0) else listOf(-model.headlightX, model.headlightX)

    /** Lueur au sol, du halo le plus large au cœur le plus lumineux. */
    fun glow(model: VehicleModel): List<BeamShape> = origins(model).flatMap { x ->
        GLOW_LAYERS.map { (length, halfWidth, opacity) ->
            BeamShape(teardrop(x, model.frontZ, length, halfWidth), baseMeters = 0.0, topMeters = 0.0, opacity = opacity)
        }
    }

    /** Volume du faisceau : plus épais près du phare, il s'aplatit en s'éloignant. */
    fun volume(model: VehicleModel): List<BeamShape> = origins(model).flatMap { x ->
        val straight = VOLUME_SLICES.map { (start, end, base, top) ->
            val footprint = listOf(
                GroundPoint(x - halfWidthAt(start), model.frontZ + start),
                GroundPoint(x + halfWidthAt(start), model.frontZ + start),
                GroundPoint(x + halfWidthAt(end), model.frontZ + end),
                GroundPoint(x - halfWidthAt(end), model.frontZ + end),
            )
            BeamShape(footprint, base, top, opacity = 1.0)
        }
        val rounded = BeamShape(
            footprint = teardrop(x, model.frontZ + LAST_SLICE_START, VOLUME_LENGTH - LAST_SLICE_START, VOLUME_HALF_WIDTH, halfWidthAt(LAST_SLICE_START)),
            baseMeters = 0.0,
            topMeters = LAST_SLICE_TOP,
            opacity = 1.0,
        )
        straight + rounded
    }

    /** Demi-largeur du volume à [distance] mètres du phare (il s'évase jusqu'à l'arrondi). */
    private fun halfWidthAt(distance: Double): Double {
        val straightLength = VOLUME_LENGTH - VOLUME_HALF_WIDTH
        val ratio = (distance / straightLength).coerceIn(0.0, 1.0)
        return NEAR_HALF_WIDTH + (VOLUME_HALF_WIDTH - NEAR_HALF_WIDTH) * ratio
    }

    /**
     * Goutte : étroite au départ ([nearHalfWidth]), elle s'évase puis se termine par un demi-cercle
     * de rayon [halfWidth], [length] mètres plus loin.
     */
    private fun teardrop(originX: Double, startZ: Double, length: Double, halfWidth: Double, nearHalfWidth: Double = NEAR_HALF_WIDTH): List<GroundPoint> {
        val arcCenterZ = startZ + length - halfWidth
        val arc = (0..ARC_STEPS).map { i ->
            val angle = PI * i / ARC_STEPS // de la droite (0) à la gauche (π), en passant par l'avant
            GroundPoint(originX + halfWidth * cos(angle), arcCenterZ + halfWidth * sin(angle))
        }
        return listOf(GroundPoint(originX - nearHalfWidth, startZ), GroundPoint(originX + nearHalfWidth, startZ)) + arc
    }
}
