package com.gamemaps.irl.data.location

import android.location.Location
import com.gamemaps.irl.core.geo.LatLng

/** Conversion d'une [Location] Android vers notre modèle [GpsFix]. */
fun Location.toGpsFix(): GpsFix = GpsFix(
    position = LatLng(latitude, longitude),
    speedMetersPerSecond = if (hasSpeed()) speed else null,
    bearingDegrees = if (hasBearing()) bearing else null,
    accuracyMeters = if (hasAccuracy()) accuracy else Float.MAX_VALUE,
    timeMillis = time,
)
