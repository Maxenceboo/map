package com.gamemaps.irl.data.speedlimit

/**
 * Limitation par défaut d'une route française dont le panneau n'est pas renseigné dans OpenStreetMap,
 * d'après son type (`highway`) et les indices d'agglomération présents dans ses étiquettes.
 * C'est une estimation : un panneau réel (70, zone 30…) peut dire autre chose.
 */
object DefaultSpeedLimits {

    private const val URBAN = 50
    private const val RURAL = 80

    /** Types de routes ouvertes aux voitures ; les autres (chemins, pistes cyclables…) sont ignorés. */
    val DRIVABLE = listOf(
        "motorway", "motorway_link", "trunk", "trunk_link",
        "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
        "unclassified", "residential", "living_street",
    )

    /** @param tags étiquettes OpenStreetMap de la route. Renvoie null pour un type non routier. */
    fun estimate(tags: Map<String, String>): Int? = when (tags["highway"]) {
        "motorway" -> 130
        "trunk" -> 110
        "motorway_link", "trunk_link" -> 90
        "living_street" -> 20
        "residential" -> URBAN
        "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified" ->
            if (looksUrban(tags)) URBAN else RURAL
        else -> null
    }

    /** En ville si une étiquette le dit, ou si la route est éclairée (rare hors agglomération). */
    private fun looksUrban(tags: Map<String, String>): Boolean {
        val hints = listOf("source:maxspeed", "maxspeed:type", "zone:maxspeed", "zone:traffic").mapNotNull { tags[it]?.lowercase() }
        return hints.any { "urban" in it || "zone" in it } || tags["lit"] == "yes"
    }
}
