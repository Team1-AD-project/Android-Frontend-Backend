package com.ecogo.app.data.repository

import com.ecogo.app.data.model.*
import com.ecogo.app.data.remote.ApiService
import com.ecogo.app.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 地图数据仓库
 * 负责处理行程追踪和路线推荐的数据逻辑
 * 实现 IMapRepository 接口，调用真实的后端 API
 */
class MapRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) : IMapRepository {

    // ========================================
    // 行程追踪相关
    // ========================================

    /**
     * 开始行程追踪
     */
    override suspend fun startTripTracking(
        userId: String,
        startPoint: GeoPoint,
        startLocation: LocationInfo?
    ): Result<TripTrackData> = withContext(Dispatchers.IO) {
        try {
            val request = TripTrackRequest(
                user_id = userId,
                start_point = startPoint,
                start_location = startLocation
            )
            val response = apiService.startTripTracking(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "开启行程失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 取消行程追踪
     */
    override suspend fun cancelTripTracking(
        tripId: String,
        userId: String,
        reason: String?
    ): Result<TripCancelData> = withContext(Dispatchers.IO) {
        try {
            val request = TripCancelRequest(
                user_id = userId,
                cancel_reason = reason
            )
            val response = apiService.cancelTripTracking(tripId, request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "取消行程失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取实时地图数据
     */
    override suspend fun getTripMap(
        tripId: String,
        userId: String
    ): Result<TripMapData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTripMap(tripId, userId)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取地图数据失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 保存行程
     */
    override suspend fun saveTrip(
        tripId: String,
        userId: String,
        endPoint: GeoPoint,
        endLocation: LocationInfo?,
        distance: Double,
        endTime: String
    ): Result<TripSaveData> = withContext(Dispatchers.IO) {
        try {
            val request = TripSaveRequest(
                trip_id = tripId,
                user_id = userId,
                end_point = endPoint,
                end_location = endLocation,
                distance = distance,
                end_time = endTime
            )
            val response = apiService.saveTrip(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "保存行程失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 计算碳足迹
     */
    override suspend fun calculateCarbon(
        tripId: String,
        transportModes: List<String>
    ): Result<CarbonCalculateData> = withContext(Dispatchers.IO) {
        try {
            val request = CarbonCalculateRequest(
                trip_id = tripId,
                transport_modes = transportModes
            )
            val response = apiService.calculateCarbon(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "计算碳足迹失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // 路线推荐相关
    // ========================================

    /**
     * 获取最低碳排路线
     */
    override suspend fun getLowestCarbonRoute(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ): Result<RouteRecommendData> = withContext(Dispatchers.IO) {
        try {
            val request = RouteRecommendRequest(
                user_id = userId,
                start_point = startPoint,
                end_point = endPoint
            )
            val response = apiService.getLowestCarbonRoute(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取路线失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取平衡路线 (时间-碳排平衡)
     */
    override suspend fun getBalancedRoute(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ): Result<RouteRecommendData> = withContext(Dispatchers.IO) {
        try {
            val request = RouteRecommendRequest(
                user_id = userId,
                start_point = startPoint,
                end_point = endPoint
            )
            val response = apiService.getBalancedRoute(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("响应数据为空"))
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取路线失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据交通方式获取路线 - 真实 API 实现（预留）
     */
    override suspend fun getRouteByTransportMode(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint,
        transportMode: TransportMode
    ): Result<RouteRecommendData> {
        // TODO: 对接后端 API
        // 暂时返回错误
        return Result.failure(Exception("API not implemented yet"))
    }
}
