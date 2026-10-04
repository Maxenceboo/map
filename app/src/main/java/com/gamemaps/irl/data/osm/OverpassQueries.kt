package com.gamemaps.irl.data.osm

import com.gamemaps.irl.data.speedlimit.DefaultSpeedLimits

import com.gamemaps.irl.core.geo.BoundingBox

/** Requêtes Overpass QL utilisées par l'application. */
object OverpassQueries {

    /** Routes ouvertes aux voitures, avec leurs étiquettes (dont `maxspeed` s'il est renseigné) et leur géométrie. */
    fun drivableRoads(box: BoundingBox): String =
        "[out:json][timeout:15];way(${bbox(box)})[highway~\"^(${DefaultSpeedLimits.DRIVABLE.joinToString("|")})$\"];out tags geom;"

    /** Radars automatiques (nœuds `highway=speed_camera`). */
    fun speedCameras(box: BoundingBox): String =
        "[out:json][timeout:15];node(${bbox(box)})[highway=speed_camera];out;"

    /** Overpass attend la bbox dans l'ordre (sud, ouest, nord, est). */
    private fun bbox(box: BoundingBox): String = "${box.south},${box.west},${box.north},${box.east}"
}
