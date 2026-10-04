package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.data.traffic.TrafficSection
import com.gamemaps.irl.data.traffic.TrafficSeverityClassifier
import org.json.JSONObject

/** Extrait les portions ralenties (`sections` de type TRAFFIC) de la réponse TomTom. */
object TomTomTrafficParser {

    /** @param lastPointIndex dernier indice valide du tracé : une section qui en sort est ignorée. */
    fun parse(sections: List<JSONObject>, lastPointIndex: Int): List<TrafficSection> = sections
        .filter { it.optString("sectionType") == "TRAFFIC" }
        .mapNotNull { section ->
            val start = section.optInt("startPointIndex", -1)
            val end = section.optInt("endPointIndex", -1)
            if (start < 0 || end <= start || end > lastPointIndex) return@mapNotNull null

            val delaySeconds = section.optDouble("delayInSeconds", 0.0)
            TrafficSection(
                startIndex = start,
                endIndex = end,
                severity = TrafficSeverityClassifier.classify(
                    speedKmh = section.optDouble("effectiveSpeedInKmh").takeUnless { it.isNaN() },
                    delaySeconds = delaySeconds,
                    magnitude = section.optInt("magnitudeOfDelay", 0),
                ),
                delaySeconds = delaySeconds,
            )
        }
}
