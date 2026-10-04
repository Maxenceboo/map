package com.gamemaps.irl.data.routing.tomtom

import org.json.JSONArray
import org.json.JSONObject

/** Les objets d'un tableau JSON ; liste vide si le tableau est absent. */
internal fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
