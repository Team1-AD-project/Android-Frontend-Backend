package com.ecogo.api

import com.ecogo.data.Activity
import com.ecogo.data.BusRoute
import com.ecogo.data.ChatRequest
import com.ecogo.data.ChatResponse
import com.ecogo.data.Faculty
import com.ecogo.data.HistoryItem
import com.ecogo.data.Ranking
import com.ecogo.data.RecommendationRequest
import com.ecogo.data.RecommendationResponse
import com.ecogo.data.Voucher
import com.ecogo.data.VoucherRedeemRequest
import com.ecogo.data.WalkingRoute
import retrofit2.http.*

/**
 * API 服务接口
 * 定义所有后端 API 端点（完全匹配后端 Controller）
 */
interface ApiService {
    
    // ==================== 活动相关 ====================
    
    /**
     * 获取所有活动
     * GET /api/v1/activities
     */
    @GET("api/v1/activities")
    suspend fun getAllActivities(): ApiResponse<List<Activity>>
    
    /**
     * 根据 ID 获取活动
     * GET /api/v1/activities/{id}
     */
    @GET("api/v1/activities/{id}")
    suspend fun getActivityById(@Path("id") id: String): ApiResponse<Activity>
    
    /**
     * 创建活动
     * POST /api/v1/activities
     */
    @POST("api/v1/activities")
    suspend fun createActivity(@Body activity: Activity): ApiResponse<Activity>
    
    /**
     * 更新活动
     * PUT /api/v1/activities/{id}
     */
    @PUT("api/v1/activities/{id}")
    suspend fun updateActivity(
        @Path("id") id: String,
        @Body activity: Activity
    ): ApiResponse<Activity>
    
    /**
     * 删除活动
     * DELETE /api/v1/activities/{id}
     */
    @DELETE("api/v1/activities/{id}")
    suspend fun deleteActivity(@Path("id") id: String): ApiResponse<Unit>
    
    /**
     * 根据状态获取活动
     * GET /api/v1/activities/status/{status}
     */
    @GET("api/v1/activities/status/{status}")
    suspend fun getActivitiesByStatus(@Path("status") status: String): ApiResponse<List<Activity>>
    
    /**
     * 参加活动
     * POST /api/v1/activities/{id}/join?userId={userId}
     */
    @POST("api/v1/activities/{id}/join")
    suspend fun joinActivity(
        @Path("id") activityId: String,
        @Query("userId") userId: String
    ): ApiResponse<Activity>
    
    /**
     * 退出活动
     * POST /api/v1/activities/{id}/leave?userId={userId}
     */
    @POST("api/v1/activities/{id}/leave")
    suspend fun leaveActivity(
        @Path("id") activityId: String,
        @Query("userId") userId: String
    ): ApiResponse<Activity>
    
    // ==================== 排行榜相关 ====================
    
    /**
     * 获取可用的排行榜周期
     * GET /api/v1/leaderboards/periods
     */
    @GET("api/v1/leaderboards/periods")
    suspend fun getLeaderboardPeriods(): ApiResponse<List<String>>
    
    /**
     * 获取指定周期的排名
     * GET /api/v1/leaderboards/rankings?period={period}
     */
    @GET("api/v1/leaderboards/rankings")
    suspend fun getRankingsByPeriod(@Query("period") period: String): ApiResponse<List<Ranking>>
    
    // ==================== 商品相关 ====================
    
    /**
     * 获取所有商品
     * GET /api/v1/goods?page=1&size=20
     */
    @GET("api/v1/goods")
    suspend fun getAllGoods(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("category") category: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("isForRedemption") isForRedemption: Boolean? = null
    ): ApiResponse<GoodsResponse>
    
    /**
     * 获取商品详情
     * GET /api/v1/goods/{id}
     */
    @GET("api/v1/goods/{id}")
    suspend fun getGoodsById(@Path("id") id: String): ApiResponse<GoodsDto>
    
    /**
     * Mobile端 - 获取可兑换商品
     * GET /api/v1/goods/mobile/redemption?vipLevel={vipLevel}
     */
    @GET("api/v1/goods/mobile/redemption")
    suspend fun getRedemptionGoods(@Query("vipLevel") vipLevel: Int? = null): ApiResponse<List<GoodsDto>>
    
    // ==================== 订单相关 ====================
    
    /**
     * 创建订单
     * POST /api/v1/orders
     */
    @POST("api/v1/orders")
    suspend fun createOrder(@Body order: OrderCreateRequest): ApiResponse<OrderDto>
    
    /**
     * 创建兑换订单
     * POST /api/v1/orders/redemption
     */
    @POST("api/v1/orders/redemption")
    suspend fun createRedemptionOrder(@Body order: OrderCreateRequest): ApiResponse<OrderDto>
    
    /**
     * 获取用户订单历史（Mobile端）
     * GET /api/v1/orders/mobile/user/{userId}?status={status}&page=1&size=10
     */
    @GET("api/v1/orders/mobile/user/{userId}")
    suspend fun getUserOrderHistory(
        @Path("userId") userId: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<OrderHistoryResponse>
    
    /**
     * 更新订单状态
     * PUT /api/v1/orders/{id}/status?status={status}
     */
    @PUT("api/v1/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: String,
        @Query("status") status: String
    ): ApiResponse<OrderDto>
    
    // ==================== 徽章相关 ====================
    
    /**
     * 购买徽章
     * POST /api/v1/mobile/badges/{badge_id}/purchase
     */
    @POST("api/v1/mobile/badges/{badge_id}/purchase")
    suspend fun purchaseBadge(
        @Path("badge_id") badgeId: String,
        @Body request: Map<String, String>
    ): ApiResponse<BadgeDto>
    
    /**
     * 切换徽章佩戴状态
     * PUT /api/v1/mobile/badges/{badge_id}/display
     */
    @PUT("api/v1/mobile/badges/{badge_id}/display")
    suspend fun toggleBadgeDisplay(
        @Path("badge_id") badgeId: String,
        @Body request: Map<String, Any>
    ): ApiResponse<BadgeDto>
    
    /**
     * 获取商店列表
     * GET /api/v1/mobile/badges/shop
     */
    @GET("api/v1/mobile/badges/shop")
    suspend fun getBadgeShopList(): ApiResponse<List<BadgeDto>>
    
    /**
     * 获取我的徽章背包
     * GET /api/v1/mobile/badges/user/{user_id}
     */
    @GET("api/v1/mobile/badges/user/{user_id}")
    suspend fun getMyBadges(@Path("user_id") userId: String): ApiResponse<List<BadgeDto>>
    
    // ==================== 统计相关 ====================
    
    /**
     * 获取仪表盘统计数据
     * GET /api/v1/statistics/dashboard
     */
    @GET("api/v1/statistics/dashboard")
    suspend fun getDashboardStats(): ApiResponse<DashboardStatsDto>

    // ==================== 路线与地图相关 ====================

    /**
     * 获取公交路线
     * GET /api/v1/routes
     */
    @GET("api/v1/routes")
    suspend fun getBusRoutes(): ApiResponse<List<BusRoute>>

    /**
     * 获取步行路线
     * GET /api/v1/walking-routes
     */
    @GET("api/v1/walking-routes")
    suspend fun getWalkingRoutes(): ApiResponse<List<WalkingRoute>>

    /**
     * 获取学院地图数据
     * GET /api/v1/faculties
     */
    @GET("api/v1/faculties")
    suspend fun getFaculties(): ApiResponse<List<Faculty>>

    /**
     * 获取兑换券列表
     * GET /api/v1/vouchers
     */
    @GET("api/v1/vouchers")
    suspend fun getVouchers(): ApiResponse<List<Voucher>>

    /**
     * 兑换券
     * POST /api/v1/vouchers/redeem
     */
    @POST("api/v1/vouchers/redeem")
    suspend fun redeemVoucher(@Body request: VoucherRedeemRequest): ApiResponse<String>

    /**
     * 获取历史记录
     * GET /api/v1/history?userId={userId}
     */
    @GET("api/v1/history")
    suspend fun getHistory(@Query("userId") userId: String? = null): ApiResponse<List<HistoryItem>>

    /**
     * 获取出行推荐
     * POST /api/v1/recommendations
     */
    @POST("api/v1/recommendations")
    suspend fun getRecommendation(@Body request: RecommendationRequest): ApiResponse<RecommendationResponse>

    /**
     * 发送聊天消息
     * POST /api/v1/chat/send
     */
    @POST("api/v1/chat/send")
    suspend fun sendChat(@Body request: ChatRequest): ApiResponse<ChatResponse>
}

// ==================== DTO 数据类 ====================

/**
 * 商品响应（带分页）
 */
data class GoodsResponse(
    val data: List<GoodsDto>,
    val pagination: PaginationDto
)

/**
 * 分页信息
 */
data class PaginationDto(
    val page: Int,
    val size: Int,
    val total: Int,
    val totalPages: Int
)

/**
 * 商品 DTO
 */
data class GoodsDto(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double?,
    val stock: Int,
    val category: String?,
    val brand: String?,
    val imageUrl: String?,
    val isActive: Boolean = true,
    val isForRedemption: Boolean = false,
    val redemptionPoints: Int = 0,
    val vipLevelRequired: Int = 0
)

/**
 * 订单创建请求
 */
data class OrderCreateRequest(
    val userId: String,
    val items: List<OrderItemRequest>,
    val shippingAddress: String? = null,
    val recipientName: String? = null,
    val recipientPhone: String? = null,
    val remark: String? = null
)

/**
 * 订单项请求
 */
data class OrderItemRequest(
    val goodsId: String,
    val goodsName: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)

/**
 * 订单 DTO
 */
data class OrderDto(
    val id: String,
    val orderNumber: String,
    val userId: String,
    val status: String,
    val totalAmount: Double,
    val shippingFee: Double,
    val finalAmount: Double,
    val paymentStatus: String,
    val createdAt: String,
    val updatedAt: String,
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val isRedemptionOrder: Boolean = false
)

/**
 * 订单历史响应（带分页）
 */
data class OrderHistoryResponse(
    val data: List<OrderSummaryDto>,
    val pagination: PaginationDto
)

/**
 * 订单摘要 DTO（简化版用于列表显示）
 */
data class OrderSummaryDto(
    val id: String,
    val orderNumber: String,
    val status: String,
    val finalAmount: Double,
    val createdAt: String,
    val itemCount: Int,
    val isRedemption: Boolean,
    val trackingNumber: String?,
    val carrier: String?
)

/**
 * 徽章 DTO
 */
data class BadgeDto(
    val id: String,
    val badgeId: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val purchaseCost: Int,
    val category: String,
    val icon: BadgeIcon,
    val isActive: Boolean
)

/**
 * 徽章图标
 */
data class BadgeIcon(
    val url: String,
    val colorScheme: String
)

/**
 * 仪表盘统计 DTO
 */
data class DashboardStatsDto(
    val totalUsers: Long,
    val activeUsers: Long,
    val totalAdvertisements: Long,
    val activeAdvertisements: Long,
    val totalActivities: Long,
    val ongoingActivities: Long,
    val totalCarbonCredits: Long,
    val totalCarbonReduction: Long,
    val redemptionVolume: Long
)
