package com.gamemaps.irl.map.theme

import androidx.annotation.DrawableRes
import com.gamemaps.irl.R

/**
 * Textures optionnelles d'un thème (images répétées en motif, sprite du véhicule).
 * null = pas de texture : la couleur de la [MapPalette] est utilisée.
 *
 * @property vehicleSprite image pixel-art affichée à la place de la flèche (non tournée).
 */
data class MapTextures(
    @DrawableRes val ground: Int? = null,
    @DrawableRes val water: Int? = null,
    @DrawableRes val woods: Int? = null,
    @DrawableRes val vehicleSprite: Int? = null,
) {
    companion object {
        val NONE = MapTextures()

        val MINECRAFT = MapTextures(
            ground = R.drawable.minecraft_grass,
            water = R.drawable.minecraft_water,
            woods = R.drawable.minecraft_tree,
            vehicleSprite = R.drawable.minecraft_pig,
        )
    }
}
