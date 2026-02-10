package com.ecogo.data

import com.ecogo.utils.TripTimeFormat


object TripMapper {

    fun toSummary(t: TripDto): TripSummaryUi {
        val startName = t.startLocation?.placeName
            ?: t.startLocation?.address
            ?: "Unknown start"

        val endName = t.endLocation?.placeName
            ?: t.endLocation?.address
            ?: "In progress"

        val route = "$startName → $endName"

        val start = safeTime(t.startTime)
        val end = if (t.endTime.isNullOrBlank()) "Now" else safeTime(t.endTime)
        val timeText = "$start ~ $end"

        val mode = t.detectedMode
            ?: t.transportModes?.firstOrNull()?.mode
            ?: "Unknown mode"


        val distKm = (t.distance ?: 0.0)   // 如果后端 distance 已经是 km，就把这行改掉
        val meta = if (t.distance == null) mode else "$mode • ${"%.2f".format(distKm)} km"

        val pts = t.pointsGained ?: 0
        val carbon = t.carbonSaved ?: 0.0
        val primary = "+$pts pts • ${"%.2f".format(carbon)} CO₂"

        val status = (t.carbonStatus ?: if (t.endTime.isNullOrBlank()) "TRACKING" else "COMPLETED").uppercase()

        return TripSummaryUi(
            id = t.id ?: "",
            routeText = route,
            timeText = timeText,
            primaryText = primary,
            metaText = meta,
            statusText = status
        )
    }

    private fun safeTime(raw: String?): String {
        return TripTimeFormat.toSgTime(raw)
    }

}
