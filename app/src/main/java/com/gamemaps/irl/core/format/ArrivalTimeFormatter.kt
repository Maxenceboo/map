package com.gamemaps.irl.core.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Heure d'arrivée estimée au format 24 h : "18:42". */
object ArrivalTimeFormatter {

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun format(nowMillis: Long, remainingSeconds: Double, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(nowMillis + (remainingSeconds * 1_000).toLong()).atZone(zone).format(formatter)
}
