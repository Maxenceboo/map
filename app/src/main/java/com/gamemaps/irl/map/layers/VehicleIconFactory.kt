package com.gamemaps.irl.map.layers

import android.content.Context
import android.graphics.Bitmap
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.theme.ThemeTextureInstaller

/**
 * Icône du véhicule selon le thème : la flèche GTA dessinée en code, ou un sprite pixel-art
 * (le cochon Minecraft), agrandi sans lissage pour garder des pixels bien nets.
 */
object VehicleIconFactory {

    /** Le sprite fait 24 px : ×4 → 96 px, la taille de la flèche. */
    private const val SPRITE_SCALE = 4

    fun create(context: Context, theme: MapTheme): Bitmap {
        val sprite = theme.textures.vehicleSprite ?: return VehicleArrowBitmap.create(theme.palette)
        val source = ThemeTextureInstaller.decodePixelArt(context, sprite)
        return Bitmap.createScaledBitmap(source, source.width * SPRITE_SCALE, source.height * SPRITE_SCALE, false)
    }
}
