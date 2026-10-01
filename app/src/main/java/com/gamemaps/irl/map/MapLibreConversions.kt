package com.gamemaps.irl.map

import com.gamemaps.irl.core.geo.LatLng
import org.maplibre.geojson.Point
import org.maplibre.android.geometry.LatLng as MapLibreLatLng

/** Seul endroit qui convertit notre [LatLng] vers les types MapLibre. */
fun LatLng.toMapLibre(): MapLibreLatLng = MapLibreLatLng(lat, lng)

/** Attention à l'ordre : GeoJSON est en (longitude, latitude). */
fun LatLng.toGeoJsonPoint(): Point = Point.fromLngLat(lng, lat)
