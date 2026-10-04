package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.map.vehicle3d.models.Arrow
import com.gamemaps.irl.map.vehicle3d.models.FormulaOne
import com.gamemaps.irl.map.vehicle3d.models.MuscleCar
import com.gamemaps.irl.map.vehicle3d.models.Motorbike
import com.gamemaps.irl.map.vehicle3d.models.SportCar
import com.gamemaps.irl.map.vehicle3d.models.Suv

/**
 * Véhicules proposés dans Paramètres > Véhicule > Modèle (cahier des charges §3.5).
 * Tous sont des modèles 3D, flèche comprise.
 */
enum class VehicleKind(val label: String, val description: String, val model: VehicleModel) {
    ARROW("Flèche radar", "Curseur en relief façon minimap GTA", Arrow.model),
    SPORT("Coupé sport", "Châssis surbaissé, aileron arrière", SportCar.model),
    MUSCLE("Muscle car", "Lignes carrées, prise d'air sur le capot", MuscleCar.model),
    SUV("4x4", "Garde au sol haute, barres de toit", Suv.model),
    F1("Formule 1", "Monoplace, ailerons et gros pneus", FormulaOne.model),
    MOTO("Moto", "Hypersport avec son pilote", Motorbike.model),
}
