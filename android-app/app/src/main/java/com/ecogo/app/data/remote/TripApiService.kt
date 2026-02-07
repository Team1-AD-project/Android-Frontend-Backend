package com.ecogo.app.data.remote

import com.ecogo.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 行程API服务接口
 * Base URL: http://47.129.124.55:8090/api/v1
 */
interface TripApiService {

    /**
     * 1. 开始行程
     * POST /mobile/trips/start
     * 返回: ResponseMessage<TripResponse>
     */
    @POST("mobile/trips/start")
    suspend fun startTrip(
        @Header("Authorization") authorization: String,
        @Body request: TripStartRequest
    ): Response<ApiResponse<TripStartResponse>>

    /**
     * 2. 完成行程
     * POST /mobile/trips/{tripId}/complete
     * 返回: ResponseMessage<TripResponse>
     */
    @POST("mobile/trips/{tripId}/complete")
    suspend fun completeTrip(
        @Path("tripId") tripId: String,
        @Header("Authorization") authorization: String,
        @Body request: TripCompleteRequest
    ): Response<ApiResponse<TripCompleteResponse>>

    /**
     * 3. 取消行程
     * POST /mobile/trips/{tripId}/cancel
     * 返回: ResponseMessage<String>
     */
    @POST("mobile/trips/{tripId}/cancel")
    suspend fun cancelTrip(
        @Path("tripId") tripId: String,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<String>>

    /**
     * 4. 获取行程列表
     * GET /mobile/trips
     * 返回: ResponseMessage<List<TripSummaryResponse>>
     */
    @GET("mobile/trips")
    suspend fun getTripList(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<TripDetail>>>

    /**
     * 5. 获取行程详情
     * GET /mobile/trips/{tripId}
     * 返回: ResponseMessage<TripResponse>
     */
    @GET("mobile/trips/{tripId}")
    suspend fun getTripDetail(
        @Path("tripId") tripId: String,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<TripDetail>>

    /**
     * 6. 获取当前追踪行程
     * GET /mobile/trips/current
     * 返回: ResponseMessage<TripResponse>
     */
    @GET("mobile/trips/current")
    suspend fun getCurrentTrip(
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<TripDetail>>

    /**
     * 7. [Admin] 获取所有行程
     * GET /web/trips/all
     * 返回: ResponseMessage<List<TripSummaryResponse>>
     */
    @GET("web/trips/all")
    suspend fun getAllTrips(
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<List<TripDetail>>>

    /**
     * 8. [Admin] 获取指定用户的行程
     * GET /web/trips/user/{userid}
     * 返回: ResponseMessage<List<TripSummaryResponse>>
     */
    @GET("web/trips/user/{userid}")
    suspend fun getUserTrips(
        @Path("userid") userId: String,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<List<TripDetail>>>
}
