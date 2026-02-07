package com.ecogo.app.data.repository

import com.ecogo.app.data.model.*

/**
 * 地图数据仓库接口
 *
 * 定义所有地图相关的数据操作
 * 真实 Repository 和 Mock Repository 都实现这个接口
 */
interface IMapRepository {

    // ========================================
    // 行程追踪相关
    // ========================================

    suspend fun startTripTracking(
        userId: String,
        startPoint: GeoPoint,
        startLocation: LocationInfo? = null
    ): Result<TripTrackData>

    suspend fun cancelTripTracking(
        tripId: String,
        userId: String,
        reason: String? = null
    ): Result<TripCancelData>

    suspend fun getTripMap(
        tripId: String,
        userId: String
    ): Result<TripMapData>

    suspend fun saveTrip(
        tripId: String,
        userId: String,
        endPoint: GeoPoint,
        endLocation: LocationInfo? = null,
        distance: Double,
        endTime: String
    ): Result<TripSaveData>

    suspend fun calculateCarbon(
        tripId: String,
        transportModes: List<String>
    ): Result<CarbonCalculateData>

    // ========================================
    // 路线推荐相关
    // ========================================

    suspend fun getLowestCarbonRoute(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ): Result<RouteRecommendData>

    suspend fun getBalancedRoute(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ): Result<RouteRecommendData>

    /**
     * 根据交通方式获取路线
     */
    suspend fun getRouteByTransportMode(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint,
        transportMode: TransportMode
    ): Result<RouteRecommendData>
}
