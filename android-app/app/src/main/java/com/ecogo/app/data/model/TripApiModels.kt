package com.ecogo.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * ========================================
 * 行程API相关模型（对接后端API）
 * Base URL: http://47.129.124.55:8090/api/v1
 * ========================================
 */

// ============================================================
// 1. 开始行程 POST /mobile/trips/start
// ============================================================

/**
 * 开始行程请求
 */
data class TripStartRequest(
    @SerializedName("startLng")
    val startLng: Double,

    @SerializedName("startLat")
    val startLat: Double,

    @SerializedName("startAddress")
    val startAddress: String,

    @SerializedName("startPlaceName")
    val startPlaceName: String,

    @SerializedName("startCampusZone")
    val startCampusZone: String? = null
)

/**
 * 开始行程响应 - 实际返回TripResponse对象
 */
typealias TripStartResponse = TripDetail

// ============================================================
// 2. 完成行程 POST /mobile/trips/{tripId}/complete
// ============================================================

/**
 * 完成行程请求
 */
data class TripCompleteRequest(
    @SerializedName("endLng")
    val endLng: Double,

    @SerializedName("endLat")
    val endLat: Double,

    @SerializedName("endAddress")
    val endAddress: String,

    @SerializedName("endPlaceName")
    val endPlaceName: String,

    @SerializedName("endCampusZone")
    val endCampusZone: String? = null,

    @SerializedName("distance")
    val distance: Double,

    @SerializedName("detectedMode")
    val detectedMode: String? = null,

    @SerializedName("mlConfidence")
    val mlConfidence: Double? = null,

    @SerializedName("isGreenTrip")
    val isGreenTrip: Boolean,

    @SerializedName("carbonSaved")
    val carbonSaved: Long,  // 单位：克(g)

    @SerializedName("transportModes")
    val transportModes: List<TransportModeSegment>,

    @SerializedName("polylinePoints")
    val polylinePoints: List<PolylinePoint>
)

/**
 * 交通方式段
 */
data class TransportModeSegment(
    @SerializedName("mode")
    val mode: String,

    @SerializedName("subDistance")
    val subDistance: Double,

    @SerializedName("subDuration")
    val subDuration: Int
)

/**
 * 路线点
 */
data class PolylinePoint(
    @SerializedName("lng")
    val lng: Double,

    @SerializedName("lat")
    val lat: Double
)

/**
 * 完成行程响应 - 实际返回TripResponse对象
 */
typealias TripCompleteResponse = TripDetail

// ============================================================
// 3. 取消行程 POST /mobile/trips/{tripId}/cancel
// ============================================================

/**
 * 取消行程响应
 */
data class TripCancelResponse(
    @SerializedName("tripId")
    val tripId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("cancelTime")
    val cancelTime: String,

    @SerializedName("message")
    val message: String? = null
)

// ============================================================
// 4. 获取行程列表 GET /mobile/trips
// ============================================================

/**
 * 行程列表响应
 */
data class TripListResponse(
    @SerializedName("trips")
    val trips: List<TripDetail>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int? = null,

    @SerializedName("pageSize")
    val pageSize: Int? = null
)

// ============================================================
// 5. 获取行程详情 GET /mobile/trips/{tripId}
// ============================================================

/**
 * 行程详情
 */
data class TripDetail(
    @SerializedName("tripId")
    val tripId: String,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("startLng")
    val startLng: Double,

    @SerializedName("startLat")
    val startLat: Double,

    @SerializedName("startAddress")
    val startAddress: String,

    @SerializedName("startPlaceName")
    val startPlaceName: String,

    @SerializedName("endLng")
    val endLng: Double? = null,

    @SerializedName("endLat")
    val endLat: Double? = null,

    @SerializedName("endAddress")
    val endAddress: String? = null,

    @SerializedName("endPlaceName")
    val endPlaceName: String? = null,

    @SerializedName("distance")
    val distance: Double? = null,

    @SerializedName("detectedMode")
    val detectedMode: String? = null,

    @SerializedName("mlConfidence")
    val mlConfidence: Double? = null,

    @SerializedName("isGreenTrip")
    val isGreenTrip: Boolean? = null,

    @SerializedName("carbonSaved")
    val carbonSaved: Long? = null,  // 单位：克(g)

    @SerializedName("pointsGained")
    val pointsGained: Long? = null,

    @SerializedName("transportModes")
    val transportModes: List<TransportModeSegment>? = null,

    @SerializedName("polylinePoints")
    val polylinePoints: List<PolylinePoint>? = null
)

// ============================================================
// 6. 获取当前追踪行程 GET /mobile/trips/current
// ============================================================

/**
 * 当前行程响应
 */
data class CurrentTripResponse(
    @SerializedName("hasCurrentTrip")
    val hasCurrentTrip: Boolean,

    @SerializedName("trip")
    val trip: TripDetail? = null
)

// ============================================================
// 通用响应包装 (ApiResponse 已移至 ApiResponse.kt)
// ============================================================

/**
 * API错误响应
 */
data class ApiError(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("details")
    val details: String? = null
)
