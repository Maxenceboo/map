package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.map.vehicle3d.models.Arrow
import com.gamemaps.irl.map.vehicle3d.models.FormulaOne
import com.gamemaps.irl.map.vehicle3d.models.Motorbike
import com.gamemaps.irl.map.vehicle3d.models.MuscleCar
import com.gamemaps.irl.map.vehicle3d.models.SportCar
import com.gamemaps.irl.map.vehicle3d.models.Suv

/**
 * Un véhicule proposé dans Paramètres > Véhicule > Modèle (cahier des charges §3.5), toujours en 3D.
 * Les véhicules fournis sont dans [entries] ; ceux créés en mode développeur s'y ajoutent
 * (voir `CustomVehicleSpec`).
 *
 * @property id identifiant stable, enregistré dans les réglages.
 * @property isCustom true pour un véhicule créé par l'utilisateur.
 */
data class VehicleKind(
    val id: String,
    val label: String,
    val description: String,
    val model: VehicleModel,
    val isCustom: Boolean = false,
) {
    override fun toString(): String = id

    companion object {
        val ARROW = VehicleKind("ARROW", "Flèche radar", "Curseur en relief façon mini-carte de jeu", Arrow.model)
        val SPORT = VehicleKind("SPORT", "Coupé sport", "Châssis surbaissé, aileron arrière", SportCar.model)
        val MUSCLE = VehicleKind("MUSCLE", "Muscle car", "Lignes carrées, prise d'air sur le capot", MuscleCar.model)
        val SUV = VehicleKind("SUV", "4x4", "Garde au sol haute, barres de toit", Suv.model)
        val F1 = VehicleKind("F1", "Monoplace", "Voiture de course, ailerons et gros pneus", FormulaOne.model)
        val MOTO = VehicleKind("MOTO", "Moto", "Hypersport avec son pilote", Motorbike.model)

        /** Véhicules fournis avec l'application. */
        val entries: List<VehicleKind> = listOf(ARROW, SPORT, MUSCLE, SUV, F1, MOTO)
    }
}
