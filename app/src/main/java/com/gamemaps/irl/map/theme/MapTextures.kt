package com.gamemaps.irl.map.theme

import androidx.annotation.DrawableRes
import com.gamemaps.irl.R
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.models.MinecraftPig

/**
 * Habillage optionnel d'un thème : images répétées en motif et véhicule imposé.
 * null = pas de texture : la couleur de la [MapPalette] est utilisée.
 *
 * @property vehicleModel véhicule 3D propre au thème (cochon Minecraft), affiché à la place
 *   de celui choisi dans les Paramètres ; null = le véhicule de l'utilisateur.
 */
data class MapTextures(
    @DrawableRes val ground: Int? = null,
    @DrawableRes val water: Int? = null,
    @DrawableRes val woods: Int? = null,
    val vehicleModel: VehicleModel? = null,
) {
    companion object {
        val NONE = MapTextures()

        val MINECRAFT = MapTextures(
            ground = R.drawable.cubic_grass,
            water = R.drawable.cubic_water,
            woods = R.drawable.cubic_tree,
            vehicleModel = MinecraftPig.model,
        )
    }
}
