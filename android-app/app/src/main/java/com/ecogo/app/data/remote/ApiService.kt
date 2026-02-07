package com.ecogo.app.data.remote

import com.ecogo.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * EcoGo 后端 API 接口
 * 基于 api接口v5.md 文档定义
 */
interface ApiService {

    // ========================================
    // TripService - 行程与碳排微服务
    // ========================================

    /**
     * 开启/追踪行程
     * POST /api/mobile/trips/track
     */
    @POST("api/mobile/trips/track")
    suspend fun startTripTracking(
        @Body request: TripTrackRequest
    ): Response<ApiResponse<TripTrackData>>

    /**
     * 取消行程追踪
     * PUT /api/mobile/trips/{trip_id}/cancel
     */
    @PUT("api/mobile/trips/{trip_id}/cancel")
    suspend fun cancelTripTracking(
        @Path("trip_id") tripId: String,
        @Body request: TripCancelRequest
    ): Response<ApiResponse<TripCancelData>>

    /**
     * 查看实时地图
     * GET /api/mobile/trips/{trip_id}/map
     */
    @GET("api/mobile/trips/{trip_id}/map")
    suspend fun getTripMap(
        @Path("trip_id") tripId: String,
        @Query("user_id") userId: String
    ): Response<ApiResponse<TripMapData>>

    /**
     * 存储行程数据
     * POST /api/mobile/trips/save
     */
    @POST("api/mobile/trips/save")
    suspend fun saveTrip(
        @Body request: TripSaveRequest
    ): Response<ApiResponse<TripSaveData>>

    /**
     * 计算行程碳足迹
     * POST /api/mobile/trips/carbon/calculate
     */
    @POST("api/mobile/trips/carbon/calculate")
    suspend fun calculateCarbon(
        @Body request: CarbonCalculateRequest
    ): Response<ApiResponse<CarbonCalculateData>>

    // ========================================
    // RecommendService - 智能推荐微服务
    // ========================================

    /**
     * 推荐碳排最低路线
     * POST /api/mobile/route/recommend/low-carbon
     */
    @POST("api/mobile/route/recommend/low-carbon")
    suspend fun getLowestCarbonRoute(
        @Body request: RouteRecommendRequest
    ): Response<ApiResponse<RouteRecommendData>>

    /**
     * 推荐时间-碳排平衡路线
     * POST /api/mobile/route/recommend/balance
     */
    @POST("api/mobile/route/recommend/balance")
    suspend fun getBalancedRoute(
        @Body request: RouteRecommendRequest
    ): Response<ApiResponse<RouteRecommendData>>

    /**
     * 查询缓存的路线推荐结果
     * GET /api/mobile/route/cache/{user_id}
     */
    @GET("api/mobile/route/cache/{user_id}")
    suspend fun getRouteCache(
        @Path("user_id") userId: String
    ): Response<ApiResponse<RouteCacheData>>
}
