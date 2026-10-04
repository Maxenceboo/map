package com.gamemaps.irl.data.speedlimit

/**
 * Limitation de la route actuelle.
 *
 * @property estimated true si elle est déduite du type de route (aucun panneau renseigné
 *   dans OpenStreetMap) : elle est affichée comme une estimation et ne déclenche pas de bip.
 */
data class SpeedLimit(val kmh: Int, val estimated: Boolean)
