package com.ecogo.repository

import com.ecogo.api.*
import com.ecogo.data.Activity
import com.ecogo.data.BusRoute
import com.ecogo.data.CarbonFootprint
import com.ecogo.data.ChatRequest
import com.ecogo.data.ChatResponse
import com.ecogo.data.CheckInResponse
import com.ecogo.data.CheckInStatus
import com.ecogo.data.DailyGoal
import com.ecogo.data.Faculty
import com.ecogo.data.Friend
import com.ecogo.data.FriendActivity
import com.ecogo.data.HistoryItem
import com.ecogo.data.MockData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * EcoGo 数据仓库
 * 统一管理所有数据访问（API 调用）
 */
class EcoGoRepository {
    
    private val api = RetrofitClient.apiService
    
    // ==================== 活动相关 ====================
    
    /**
     * 获取所有活动
     */
    suspend fun getAllActivities(): Result<List<Activity>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllActivities()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据 ID 获取活动
     */
    suspend fun getActivityById(id: String): Result<Activity> = withContext(Dispatchers.IO) {
        try {
            val response = api.getActivityById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 参加活动
     */
    suspend fun joinActivity(activityId: String, userId: String): Result<Activity> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.joinActivity(activityId, userId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 退出活动
     */
    suspend fun leaveActivity(activityId: String, userId: String): Result<Activity> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.leaveActivity(activityId, userId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // ==================== 排行榜相关 ====================
    
    /**
     * 获取排行榜数据
     */
    suspend fun getLeaderboard(period: String = "Week 4, 2026"): Result<List<Ranking>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getRankingsByPeriod(period)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取可用的排行榜周期
     */
    suspend fun getAvailablePeriods(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLeaderboardPeriods()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== 商品相关 ====================
    
    /**
     * 获取所有商品（带分页）
     */
    suspend fun getAllGoods(
        page: Int = 1,
        size: Int = 20,
        category: String? = null,
        keyword: String? = null
    ): Result<GoodsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllGoods(page, size, category, keyword, null)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取可兑换商品
     */
    suspend fun getRedemptionGoods(vipLevel: Int? = null): Result<List<GoodsDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getRedemptionGoods(vipLevel)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取商品详情
     */
    suspend fun getGoodsById(id: String): Result<GoodsDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGoodsById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== 订单相关 ====================
    
    /**
     * 创建订单
     */
    suspend fun createOrder(order: OrderCreateRequest): Result<OrderDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createOrder(order)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 创建兑换订单
     */
    suspend fun createRedemptionOrder(order: OrderCreateRequest): Result<OrderDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createRedemptionOrder(order)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取用户订单历史
     */
    suspend fun getUserOrderHistory(
        userId: String,
        status: String? = null,
        page: Int = 1,
        size: Int = 10
    ): Result<OrderHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserOrderHistory(userId, status, page, size)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== 徽章相关 ====================
    
    /**
     * 购买徽章
     */
    suspend fun purchaseBadge(userId: String, badgeId: String): Result<BadgeDto> =
        withContext(Dispatchers.IO) {
            try {
                val request = mapOf("user_id" to userId)
                val response = api.purchaseBadge(badgeId, request)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 切换徽章佩戴状态
     */
    suspend fun toggleBadgeDisplay(
        userId: String,
        badgeId: String,
        isDisplay: Boolean
    ): Result<BadgeDto> = withContext(Dispatchers.IO) {
        try {
            val request = mapOf("user_id" to userId, "is_display" to isDisplay)
            val response = api.toggleBadgeDisplay(badgeId, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取徽章商店列表
     */
    suspend fun getBadgeShopList(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBadgeShopList()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取我的徽章背包
     */
    suspend fun getMyBadges(userId: String): Result<List<BadgeDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMyBadges(userId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // ==================== 统计相关 ====================
    
    /**
     * 获取仪表盘统计数据
     */
    suspend fun getDashboardStats(): Result<DashboardStatsDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getDashboardStats()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ==================== 路线与地图相关 ====================

    suspend fun getBusRoutes(): Result<List<BusRoute>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBusRoutes()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWalkingRoutes(): Result<List<WalkingRoute>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWalkingRoutes()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFaculties(): Result<List<Faculty>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFaculties()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVouchers(): Result<List<Voucher>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getVouchers()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun redeemVoucher(request: VoucherRedeemRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.redeemVoucher(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(userId: String? = null): Result<List<HistoryItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getHistory(userId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getRecommendation(request: RecommendationRequest): Result<RecommendationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getRecommendation(request)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun sendChat(request: ChatRequest): Result<ChatResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.sendChat(request)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // ==================== 新功能 API 方法 ====================
    
    /**
     * 每日签到
     */
    suspend fun checkIn(userId: String): Result<CheckInResponse> =
        withContext(Dispatchers.IO) {
            try {
                // 模拟API调用，实际应调用真实API
                val response = CheckInResponse(
                    success = true,
                    pointsEarned = 10,
                    consecutiveDays = MockData.CHECK_IN_STATUS.consecutiveDays + 1,
                    message = "签到成功！"
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取签到状态
     */
    suspend fun getCheckInStatus(userId: String): Result<CheckInStatus> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.CHECK_IN_STATUS)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取今日目标进度
     */
    suspend fun getDailyGoal(userId: String): Result<DailyGoal> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.DAILY_GOAL)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取天气和空气质量
     */
    suspend fun getWeather(location: String = "NUS"): Result<Weather> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.WEATHER)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取通知列表
     */
    suspend fun getNotifications(userId: String): Result<List<Notification>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.NOTIFICATIONS.filter { !it.isRead })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 标记通知为已读
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取碳足迹数据
     */
    suspend fun getCarbonFootprint(userId: String, period: String = "monthly"): Result<CarbonFootprint> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.CARBON_FOOTPRINT)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取好友列表
     */
    suspend fun getFriends(userId: String): Result<List<Friend>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.FRIENDS)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取好友动态
     */
    suspend fun getFriendActivities(userId: String): Result<List<FriendActivity>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(MockData.FRIEND_ACTIVITIES)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // ==================== 商店相关 ====================
    
    /**
     * 获取商店商品列表
     */
    suspend fun getShopProducts(
        type: String? = null,
        category: String? = null
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getShopProducts(type, category)
            if (response.success && response.data != null) {
                Result.success(response.data.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取商品详情
     */
    suspend fun getProductById(productId: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductById(productId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 积分兑换商品
     */
    suspend fun redeemProduct(
        userId: String,
        productId: String,
        productType: String
    ): Result<RedeemResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RedeemRequest(userId, productId, productType)
            val response = api.redeemProduct(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建支付Intent
     */
    suspend fun createPaymentIntent(
        userId: String,
        productId: String
    ): Result<PaymentIntentResponse> = withContext(Dispatchers.IO) {
        try {
            val request = PaymentIntentRequest(userId, productId)
            val response = api.createPaymentIntent(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 确认支付
     */
    suspend fun confirmPayment(
        userId: String,
        productId: String,
        paymentIntentId: String
    ): Result<OrderDto> = withContext(Dispatchers.IO) {
        try {
            val request = ConfirmPaymentRequest(userId, productId, paymentIntentId)
            val response = api.confirmPayment(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
