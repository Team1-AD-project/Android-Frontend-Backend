package com.ecogo.app.data.model

/**
 * ========================================
 * 行程追踪相关模型
 * 对应 TripService 微服务接口
 * ========================================
 */

/**
 * 开启行程追踪请求
 * POST /api/mobile/trips/track
 */
data class TripTrackRequest(
    val user_id: String,
    val start_point: GeoPoint,
    val start_location: LocationInfo? = null
)

/**
 * 开启行程追踪响应
 */
data class TripTrackData(
    val trip_id: String,
    val status: String = "tracking",
    val start_time: String,
    val message: String? = null
)

/**
 * 取消行程请求
 * PUT /api/mobile/trips/{trip_id}/cancel
 */
data class TripCancelRequest(
    val user_id: String,
    val cancel_reason: String? = null
)

/**
 * 取消行程响应
 */
data class TripCancelData(
    val trip_id: String,
    val status: String,
    val cancel_time: String? = null,
    val message: String? = null
)

/**
 * 轨迹点
 */
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Double? = null
)

/**
 * 实时地图响应
 * GET /api/mobile/trips/{trip_id}/map
 */
data class TripMapData(
    val trip_id: String? = null,
    val track_points: List<TrackPoint> = emptyList(),
    val current_distance: Double = 0.0,
    val duration_seconds: Int = 0,
    val status: String = "tracking",
    // 兼容旧字段
    val polyline_points: List<GeoPoint>? = null,
    val current_point: GeoPoint? = null
)

/**
 * 保存行程请求
 * POST /api/mobile/trips/save
 */
data class TripSaveRequest(
    val trip_id: String,
    val user_id: String,
    val end_point: GeoPoint,
    val end_location: LocationInfo? = null,
    val distance: Double,
    val end_time: String
)

/**
 * 保存行程响应
 */
data class TripSaveData(
    val trip_id: String,
    val status: String,
    val total_distance: Double? = null,
    val duration_minutes: Int? = null,
    val message: String? = null
)

/**
 * 碳足迹计算请求
 * POST /api/mobile/trips/carbon/calculate
 */
data class CarbonCalculateRequest(
    val trip_id: String,
    val transport_modes: List<String>
)

/**
 * 碳足迹计算响应
 */
data class CarbonCalculateData(
    val trip_id: String? = null,
    val total_carbon_emission: Double = 0.0,
    val carbon_saved: Double,
    val green_points: Int = 0,
    val is_green_trip: Boolean = true,
    val transport_breakdown: Map<String, Double>? = null
)

/**
 * 行程状态枚举
 */
enum class TripStatus(val value: String) {
    TRACKING("tracking"),
    COMPLETED("completed"),
    CANCELED("canceled")
}
