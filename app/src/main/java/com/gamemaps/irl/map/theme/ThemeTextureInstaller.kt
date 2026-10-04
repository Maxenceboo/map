package com.gamemaps.irl.map.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.maplibre.android.maps.Style

/**
 * Charge les textures d'un thème et les ajoute au style comme images de motif.
 * Les identifiants sont fixes : changer de thème remplace simplement les images.
 */
object ThemeTextureInstaller {

    private const val GROUND_ID = "theme-ground"
    private const val WATER_ID = "theme-water"
    private const val WOODS_ID = "theme-woods"

    fun install(context: Context, style: Style, textures: MapTextures): ThemePatterns = ThemePatterns(
        ground = textures.ground?.let { add(context, style, GROUND_ID, it) },
        water = textures.water?.let { add(context, style, WATER_ID, it) },
        woods = textures.woods?.let { add(context, style, WOODS_ID, it) },
    )

    private fun add(context: Context, style: Style, id: String, drawableRes: Int): String {
        style.addImage(id, decodePixelArt(context, drawableRes))
        return id
    }

    /** Décode sans mise à l'échelle ni lissage : les pixels Minecraft doivent rester nets. */
    fun decodePixelArt(context: Context, drawableRes: Int): Bitmap =
        BitmapFactory.decodeResource(context.resources, drawableRes, BitmapFactory.Options().apply { inScaled = false })
}
