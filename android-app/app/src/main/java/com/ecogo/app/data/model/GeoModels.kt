package com.ecogo.app.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * 经纬度坐标点
 * 对应 API: {"lng": number, "lat": number}
 */
data class GeoPoint(
    val lng: Double,
    val lat: Double
) {
    fun toLatLng(): LatLng = LatLng(lat, lng)

    companion object {
        fun fromLatLng(latLng: LatLng): GeoPoint = GeoPoint(latLng.longitude, latLng.latitude)
    }
}

/**
 * 位置详情
 * 对应 API: {"address": "string", "place_name": "string", "campus_zone": "string"}
 */
data class LocationInfo(
    val address: String? = null,
    val place_name: String? = null,
    val campus_zone: String? = null
)

/**
 * 路线点集合 (用于绘制 Polyline)
 */
data class RoutePoints(
    val points: List<GeoPoint>
) {
    fun toLatLngList(): List<LatLng> = points.map { it.toLatLng() }
}
