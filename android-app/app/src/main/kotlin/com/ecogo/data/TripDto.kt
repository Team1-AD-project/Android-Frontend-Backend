package com.ecogo.data

data class TripDto(
    val id: String? = null,
    val userId: String? = null,

    val startPoint: GeoPoint? = null,
    val endPoint: GeoPoint? = null,

    val startLocation: PlaceLocation? = null,
    val endLocation: PlaceLocation? = null,

    val startTime: String? = null,
    val endTime: String? = null,

    val transportModes: List<TransportSegmentDto>? = null,
    val detectedMode: String? = null,
    val mlConfidence: Double? = null,

    val isGreenTrip: Boolean? = null,
    val distance: Double? = null,

    // 你 Apifox 里它经常为 null，真实类型不确定，先 Any? 最安全
    val polylinePoints: Any? = null,

    val carbonSaved: Double? = null,
    val pointsGained: Int? = null,
    val carbonStatus: String? = null,
    val createdAt: String? = null
)

data class GeoPoint(
    val lng: Double? = null,
    val lat: Double? = null
)

data class PlaceLocation(
    val address: String? = null,
    val placeName: String? = null,
    val campusZone: String? = null
)

data class TransportSegmentDto(
    val mode: String? = null,
    val subDistance: Double? = null,
    val subDuration: Int? = null
)

