package com.ecogo.api

import com.ecogo.data.Activity
import com.ecogo.data.BusRoute
import com.ecogo.data.CarbonFootprint
import com.ecogo.data.ChatRequest
import com.ecogo.data.ChatResponse
import com.ecogo.data.CheckIn
import com.ecogo.data.CheckInResponse
import com.ecogo.data.CheckInStatus
import com.ecogo.data.DailyGoal
import com.ecogo.data.Faculty
import com.ecogo.data.Friend
import com.ecogo.data.FriendActivity
import com.ecogo.data.HistoryItem
import com.ecogo.data.Notification
import com.ecogo.data.Product
import com.ecogo.data.Ranking
import com.ecogo.data.RecommendationRequest
import com.ecogo.data.RecommendationResponse
import com.ecogo.data.RedeemRequest
import com.ecogo.data.RedeemResponse
import com.ecogo.data.Voucher
import com.ecogo.data.VoucherRedeemRequest
import com.ecogo.data.WalkingRoute
import com.ecogo.data.Weather
import com.google.gson.annotations.SerializedName
import retrofit2.http.*

/**
 * API 服务接口
 * 定义所有后端 API 端点（完全匹配后端 Controller）
 */
interface ApiService {
    
    // ==================== 活动相关 ====================
    
    /**
     * 获取所有活动 (Mobile)
     * GET /api/v1/mobile/activities
     */
    @GET("api/v1/mobile/activities")
    suspend fun getAllActivities(): ApiResponse<List<Activity>>
    
    /**
     * 根据 ID 获取活动 (Mobile)
     * GET /api/v1/mobile/activities/{id}
     */
    @GET("api/v1/mobile/activities/{id}")
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
     * 参加活动 (Mobile)
     * POST /api/v1/mobile/activities/{id}/join?userId={userId}
     */
    @POST("api/v1/mobile/activities/{id}/join")
    suspend fun joinActivity(
        @Path("id") activityId: String,
        @Query("userId") userId: String
    ): ApiResponse<Activity>

    /**
     * 退出活动 (Mobile)
     * POST /api/v1/mobile/activities/{id}/leave?userId={userId}
     */
    @POST("api/v1/mobile/activities/{id}/leave")
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
    
    // ==================== 签到相关 ====================
    
    /**
     * 执行签到
     * POST /api/v1/checkin?userId={userId}
     */
    @POST("api/v1/checkin")
    suspend fun performCheckIn(@Query("userId") userId: String): ApiResponse<CheckInResponse>
    
    /**
     * 获取签到状态
     * GET /api/v1/checkin/status/{userId}
     */
    @GET("api/v1/checkin/status/{userId}")
    suspend fun getCheckInStatus(@Path("userId") userId: String): ApiResponse<CheckInStatus>
    
    /**
     * 获取签到历史
     * GET /api/v1/checkin/history/{userId}
     */
    @GET("api/v1/checkin/history/{userId}")
    suspend fun getCheckInHistory(@Path("userId") userId: String): ApiResponse<List<CheckIn>>
    
    // ==================== 每日目标相关 ====================
    
    /**
     * 获取今日目标
     * GET /api/v1/goals/daily/{userId}
     */
    @GET("api/v1/goals/daily/{userId}")
    suspend fun getDailyGoal(@Path("userId") userId: String): ApiResponse<DailyGoal>
    
    /**
     * 更新今日目标
     * PUT /api/v1/goals/daily/{userId}
     */
    @PUT("api/v1/goals/daily/{userId}")
    suspend fun updateDailyGoal(
        @Path("userId") userId: String,
        @Body updates: Map<String, Any>
    ): ApiResponse<DailyGoal>
    
    // ==================== 碳足迹相关 ====================
    
    /**
     * 获取碳足迹数据
     * GET /api/v1/carbon/{userId}?period={period}
     */
    @GET("api/v1/carbon/{userId}")
    suspend fun getCarbonFootprint(
        @Path("userId") userId: String,
        @Query("period") period: String = "monthly"
    ): ApiResponse<CarbonFootprint>
    
    /**
     * 记录出行
     * POST /api/v1/carbon/record
     */
    @POST("api/v1/carbon/record")
    suspend fun recordTrip(@Body request: Map<String, Any>): ApiResponse<CarbonFootprint>
    
    // ==================== 通知相关 ====================
    
    /**
     * 获取用户通知
     * GET /api/v1/notifications/{userId}
     */
    @GET("api/v1/notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): ApiResponse<List<Notification>>
    
    /**
     * 获取未读通知
     * GET /api/v1/notifications/{userId}/unread
     */
    @GET("api/v1/notifications/{userId}/unread")
    suspend fun getUnreadNotifications(@Path("userId") userId: String): ApiResponse<List<Notification>>
    
    /**
     * 标记通知为已读
     * POST /api/v1/notifications/{notificationId}/read
     */
    @POST("api/v1/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: String): ApiResponse<Notification>
    
    /**
     * 标记所有通知为已读
     * POST /api/v1/notifications/{userId}/read-all
     */
    @POST("api/v1/notifications/{userId}/read-all")
    suspend fun markAllNotificationsAsRead(@Path("userId") userId: String): ApiResponse<Unit>
    
    // ==================== 好友相关 ====================
    
    /**
     * 获取好友列表
     * GET /api/v1/friends/{userId}
     */
    @GET("api/v1/friends/{userId}")
    suspend fun getFriends(@Path("userId") userId: String): ApiResponse<List<Friend>>
    
    /**
     * 添加好友
     * POST /api/v1/friends/add
     */
    @POST("api/v1/friends/add")
    suspend fun addFriend(@Body request: Map<String, String>): ApiResponse<Friend>
    
    /**
     * 删除好友
     * DELETE /api/v1/friends/{userId}/{friendId}
     */
    @DELETE("api/v1/friends/{userId}/{friendId}")
    suspend fun removeFriend(
        @Path("userId") userId: String,
        @Path("friendId") friendId: String
    ): ApiResponse<Unit>
    
    /**
     * 获取好友请求
     * GET /api/v1/friends/requests/{userId}
     */
    @GET("api/v1/friends/requests/{userId}")
    suspend fun getFriendRequests(@Path("userId") userId: String): ApiResponse<List<Friend>>
    
    /**
     * 接受好友请求
     * POST /api/v1/friends/accept
     */
    @POST("api/v1/friends/accept")
    suspend fun acceptFriendRequest(@Body request: Map<String, String>): ApiResponse<Friend>
    
    /**
     * 获取好友动态
     * GET /api/v1/friends/{userId}/activities
     */
    @GET("api/v1/friends/{userId}/activities")
    suspend fun getFriendActivities(@Path("userId") userId: String): ApiResponse<List<FriendActivity>>
    
    // ==================== 天气相关（可选，使用第三方API） ====================
    
    /**
     * 获取天气数据
     * GET /api/v1/weather
     */
    @GET("api/v1/weather")
    suspend fun getWeather(): ApiResponse<Weather>
    
    // ==================== 商店相关 ====================
    
    /**
     * 获取所有商品
     * GET /api/v1/shop/products
     */
    @GET("api/v1/shop/products")
    suspend fun getShopProducts(
        @Query("type") type: String? = null,  // "voucher", "goods", "all"
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<ProductsResponse>
    
    /**
     * 获取单个商品详情
     * GET /api/v1/shop/products/{id}
     */
    @GET("api/v1/shop/products/{id}")
    suspend fun getProductById(@Path("id") id: String): ApiResponse<Product>
    
    /**
     * 积分兑换
     * POST /api/v1/shop/redeem
     */
    @POST("api/v1/shop/redeem")
    suspend fun redeemProduct(@Body request: RedeemRequest): ApiResponse<RedeemResponse>
    
    /**
     * 创建PaymentIntent
     * POST /api/v1/shop/payment-intent
     */
    @POST("api/v1/shop/payment-intent")
    suspend fun createPaymentIntent(@Body request: PaymentIntentRequest): ApiResponse<PaymentIntentResponse>
    
    /**
     * 确认支付
     * POST /api/v1/shop/confirm-payment
     */
    @POST("api/v1/shop/confirm-payment")
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequest): ApiResponse<OrderDto>
    /**
     * 移动端用户登录
     * POST /api/v1/mobile/users/login
     */
    @POST("api/v1/mobile/users/login")
    suspend fun login(@Body request: MobileLoginRequest): ApiResponse<MobileLoginResponse>

    /**
     * 获取用户信息 (包含VIP状态)
     * GET /api/v1/mobile/users/{id}
     */
    @GET("api/v1/mobile/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): ApiResponse<UserInfo>


    /**
     * 获取移动端用户详细资料
     * GET /api/v1/mobile/users/profile
     */
    /**
     * 获取移动端用户详细资料
     * GET /api/v1/mobile/users/profile
     */
    @GET("api/v1/mobile/users/profile")
    suspend fun getMobileUserProfile(): ApiResponse<MobileProfileResponse>

    /**
     * 获取当前积分
     * GET /api/v1/mobile/points/current
     */
    @GET("api/v1/mobile/points/current")
    suspend fun getCurrentPoints(): ApiResponse<CurrentPointsResponse>

    /**
     * 获取用户积分历史
     * GET /api/v1/mobile/points/history
     */
    @GET("api/v1/mobile/points/history")
    suspend fun getMobilePointsHistory(): ApiResponse<List<PointHistoryItem>>

    @POST("api/v1/mobile/users/register")
    suspend fun register(@Body request: MobileRegisterRequest): ApiResponse<MobileRegisterData>

    /**
     * 更新用户资料 (Internal API - No Token)
     * PUT /api/v1/internal/users/{userid}/profile
     * Note: ideally this should be the authenticated /api/v1/mobile/users/profile endpoint for PUT too,
     * but for now we keep the wizard flow as is or migrate if needed.
     */
    @PUT("api/v1/internal/users/{userid}/profile")
    suspend fun updateProfile(
        @Path("userid") userId: String,
        @Body request: UpdateProfileRequest
    ): ApiResponse<Any>
}

// ==================== DTO 数据类 ====================


/**
 * 移动端登录请求
 */
data class MobileLoginRequest(
    val userid: String,
    val password: Any 
)

/**
 * 移动端登录响应
 */
data class MobileLoginResponse(
    val token: String,
    @SerializedName("user_info") val userInfo: UserInfo
)

data class MobileProfileResponse(
    @SerializedName("user_info") val userInfo: UserInfo,
    @SerializedName("vip_info") val vipInfo: VipInfo?,
    val stats: UserStats?
)

data class UserInfo(
    val id: String,
    val userid: String,
    val email: String?,
    val phone: String?,
    val nickname: String,
    val avatar: String?,
    val vip: VipInfo? = null,
    val stats: UserStats? = null,
    val preferences: UserPreferences? = null,
    val activityMetrics: ActivityMetrics? = null,
    val totalCarbon: Double = 0.0,
    val totalPoints: Int = 0,
    val currentPoints: Int = 0,
    val lastLoginAt: String?,
    val createdAt: String?,
    val isAdmin: Boolean = false,
    val faculty: String? = null // Added based on context, though optional in sample
)

data class UserStats(
    val totalTrips: Int,
    val totalDistance: Double,
    val greenDays: Int,
    val weeklyRank: Int,
    val monthlyRank: Int,
    val totalPointsFromTrips: Int
)

data class UserPreferences(
    val preferredTransport: List<String>?,
    val enablePush: Boolean,
    val enableEmail: Boolean,
    val enableBusReminder: Boolean,
    val language: String,
    val theme: String,
    val shareLocation: Boolean,
    val showOnLeaderboard: Boolean,
    val shareAchievements: Boolean
)

data class ActivityMetrics(
    val activeDays7d: Int,
    val activeDays30d: Int,
    val lastTripDays: Int,
    val loginFrequency7d: Int
)

data class VipInfo(
    val active: Boolean,
    val plan: String?,
    val expiryDate: String?,
    val autoRenew: Boolean = false,
    val pointsMultiplier: Double = 1.0
)

/**
 * 移动端注册请求
 */
data class MobileRegisterRequest(
    val userid: String,
    val password: String,
    val nickname: String,
    val repassword: String,
    val email: String
)

/**
 * 移动端注册响应
 */
data class MobileRegisterData(
    val id: String,
    val userid: String,
    val nickname: String,
    @SerializedName("created_at") val createdAt: String
)

/**
 * 更新个人资料请求 (支持部分更新)
 */
data class UpdateProfileRequest(
    val nickname: String? = null,
    val faculty: String? = null,
    val preferences: TransportPreferencesWrapper? = null,
    val dormitoryOrResidence: String? = null,
    val mainTeachingBuilding: String? = null,
    val favoriteStudySpot: String? = null,
    val interests: List<String>? = null,
    val weeklyGoals: Int? = null,
    val newChallenges: Boolean? = null,
    val activityReminders: Boolean? = null,
    val friendActivity: Boolean? = null
)

data class TransportPreferencesWrapper(
    val preferredTransport: List<String>
)


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

/**
 * 当前积分响应
 */
data class CurrentPointsResponse(
    val userId: String,
    val currentPoints: Long,
    val totalPoints: Long
)

/**
 * 商品响应（带分页）
 */
data class ProductsResponse(
    val data: List<Product>,
    val pagination: PaginationDto
)

/**
 * 支付Intent请求
 */
data class PaymentIntentRequest(
    val userId: String,
    val productId: String
)

/**
 * 支付Intent响应
 */
data class PaymentIntentResponse(
    val clientSecret: String,
    val publishableKey: String
)

data class PointHistoryItem(
    val id: String,
    @SerializedName("change_type") val changeType: String,
    val points: Int,
    val source: String,
    val description: String?,
    @SerializedName("balance_after") val balanceAfter: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("admin_action") val adminAction: AdminAction?
)

data class AdminAction(
    @SerializedName("operator_id") val operatorId: String,
    val reason: String,
    @SerializedName("approval_status") val approvalStatus: String
)

/**
 * 确认支付请求
 */
data class ConfirmPaymentRequest(
    val userId: String,
    val productId: String,
    val paymentIntentId: String
)
