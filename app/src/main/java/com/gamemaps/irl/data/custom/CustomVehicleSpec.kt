package com.gamemaps.irl.data.custom

import com.gamemaps.irl.map.vehicle3d.VehicleKind

/**
 * Un véhicule créé en mode développeur, décrit par ses mesures (en mètres).
 * [CustomVehicleBuilder] en fait un modèle 3D.
 *
 * @property bodyHeight hauteur de la caisse, sans l'habitacle.
 * @property groundClearance garde au sol.
 * @property cabinRatio longueur de l'habitacle, en part de la longueur totale.
 * @property cabinHeight hauteur des vitres.
 */
data class CustomVehicleSpec(
    val id: String,
    val name: String,
    val length: Double = 4.4,
    val width: Double = 1.9,
    val bodyHeight: Double = 0.55,
    val groundClearance: Double = 0.25,
    val cabinRatio: Double = 0.45,
    val cabinHeight: Double = 0.5,
    val wheelDiameter: Double = 0.68,
    val spoiler: Boolean = false,
) {
    /** Les mesures ramenées dans les limites de l'éditeur (un fichier abîmé ne casse pas l'affichage). */
    fun clamped() = copy(
        length = length.coerceIn(LENGTH),
        width = width.coerceIn(WIDTH),
        bodyHeight = bodyHeight.coerceIn(BODY_HEIGHT),
        groundClearance = groundClearance.coerceIn(GROUND_CLEARANCE),
        cabinRatio = cabinRatio.coerceIn(CABIN_RATIO),
        cabinHeight = cabinHeight.coerceIn(CABIN_HEIGHT),
        wheelDiameter = wheelDiameter.coerceIn(WHEEL_DIAMETER),
    )

    fun toVehicleKind() = VehicleKind(
        id = id,
        label = name.ifBlank { "Véhicule sans nom" },
        description = "Véhicule personnalisé",
        model = CustomVehicleBuilder.build(this),
        isCustom = true,
    )

    companion object {
        val LENGTH = 2.6..5.8
        val WIDTH = 1.4..2.3
        val BODY_HEIGHT = 0.3..1.2
        val GROUND_CLEARANCE = 0.1..0.7
        val CABIN_RATIO = 0.25..0.7
        val CABIN_HEIGHT = 0.25..0.9
        val WHEEL_DIAMETER = 0.5..1.1
    }
}
