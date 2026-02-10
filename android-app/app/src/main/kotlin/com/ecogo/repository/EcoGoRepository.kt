package com.ecogo.repository

import com.ecogo.api.*
import com.ecogo.auth.TokenManager.isVipActive
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
import com.ecogo.data.FacultyCarbonData
import com.ecogo.data.Friend
import com.ecogo.data.FriendActivity
import com.ecogo.data.HistoryItem
import com.ecogo.data.MobileOrderHistoryData
import com.ecogo.data.MockData
import com.ecogo.data.Notification
import com.ecogo.data.Product
import com.ecogo.data.Ranking
import com.ecogo.data.RecommendationRequest
import com.ecogo.data.RecommendationResponse
import com.ecogo.data.RedeemRequest
import com.ecogo.data.RedeemResponse
import com.ecogo.data.UserVoucher
import com.ecogo.data.Voucher
import com.ecogo.data.VoucherRedeemRequest
import com.ecogo.data.WalkingRoute
import com.ecogo.data.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.ecogo.data.PointsCurrentData
import com.ecogo.data.TripDto


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
     * 获取学院月度碳排放统计排名（从数据库实时计算）
     */
    suspend fun getFacultyMonthlyCarbonStats(): Result<List<com.ecogo.data.FacultyCarbonData>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getFacultyMonthlyCarbonStats()
                if (response.success && response.data != null) {
                    // Sort descending by totalCarbon for ranking
                    val sorted = response.data.sortedByDescending { it.totalCarbon }
                    Result.success(sorted)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load faculty stats"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 获取个人排行榜数据（DAILY 或 MONTHLY，移动端只能看今天/本月）
     */
    suspend fun getIndividualRankings(type: String): Result<com.ecogo.data.LeaderboardStatsData> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMobileLeaderboardRankings(type)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load rankings"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // ==================== 商品相关 ====================
    suspend fun getUserTripHistoryWeb(userId: String): Result<List<TripDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserTripsWeb(userId)

            // ✅ 按你后端 web trips 返回的 code/message/data 结构处理
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load trips"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyTripHistory(): Result<List<TripDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getMyTripHistory()
            if (resp.code == 200 && resp.data != null) {
                Result.success(resp.data)
            } else {
                Result.failure(Exception(resp.message ?: "Failed to load trip history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getCurrentPoints(): Result<PointsCurrentData> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = api.getCurrentPoints()
            if (resp.success && resp.data != null) {
                Result.success(resp.data)
            } else {
                Result.failure(Exception(resp.message ?: "Failed to load points"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserOrderHistoryMobile(
        userId: String,
        status: String? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<MobileOrderHistoryData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserOrderHistoryMobile(
                userId = userId,
                status = status,
                page = page,
                size = size
            )

            // ✅ 统一后的 code/message/data 结构
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    /**
     * 获取所有商品（带分页）
     */
    suspend fun getAllGoods(
        page: Int = 1,
        size: Int = 20,
        category: String? = null,
        keyword: String? = null,
        isForRedemption: Boolean? = null,
        isVipActive: Boolean? = null
    ): Result<GoodsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllGoods(
                page = page,
                size = size,
                category = category,
                keyword = keyword,
                isForRedemption = isForRedemption,
                isVipActive = isVipActive
            )
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

    suspend fun getVouchers(isVipActive: Boolean? = null): Result<List<Voucher>> =
        withContext(Dispatchers.IO) {
            try {
                // ✅ 改为调用你后端的 coupons 接口
                val response = api.getGoodsCoupons(isVipActive)

                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getUserVouchers(userId: String, tab: String): Result<List<UserVoucher>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUserVouchers(userId, tab)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getUserVoucherDetail(userVoucherId: String): Result<UserVoucher> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUserVoucherById(userVoucherId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchMyChurnRisk(userId: String): String? {
        val resp = api.getMyChurnRisk(userId)
        return if (resp.code == 200) resp.data?.riskLevel else null
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

     * 获取用户资料 (Internal API)
     */
    suspend fun getUserProfile(userId: String): Result<UserInfo> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUserProfile(userId)
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
     * 更新用户资料
     * PUT /api/v1/internal/users/{userid}/profile
     */

    suspend fun updateUserProfile(userId: String, request: com.ecogo.api.UpdateProfileRequest): Result<Any> =
        withContext(Dispatchers.IO) {
            try {
                // Modified: Now calls profile endpoint which infers user from token
                val response = api.updateProfile(request)
                if (response.success) {
                    Result.success(response.data ?: Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 更新用户资料
     */
    suspend fun updateInternalUserProfile(userId: String, request: com.ecogo.api.UpdateProfileRequest): Result<Any> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateInternalUserProfile(userId, request)
                if (response.success) {
                    Result.success(response.data ?: Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 获取移动端用户详细资料 (Authenticated)
     */
    // 统一参数：保留无参版本（匹配api.getMobileUserProfile()调用），避免参数不一致报错
    suspend fun getMobileUserProfile(): Result<MobileProfileResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMobileUserProfile()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMobilePointsHistory(): Result<List<com.ecogo.api.PointHistoryItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMobilePointsHistory()
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
     * 获取院系积分统计
     */
    suspend fun getFacultyPointsStats(): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getFacultyPointsStats()
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
     * 获取用户已加入的活动数量
     * 遍历所有活动，筛选包含当前用户ID的活动
     */
    suspend fun getJoinedActivitiesCount(userId: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAllActivities()
                if (response.success && response.data != null) {
                    val joinedCount = response.data.count { activity ->
                        activity.participantIds.contains(userId)
                    }
                    Result.success(joinedCount)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 获取用户已加入的挑战数量
     */
    suspend fun getJoinedChallengesCount(userId: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUserChallenges(userId)
                if (response.success && response.data != null) {
                    Result.success(response.data.size)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 获取用户已加入的挑战列表
     */
    suspend fun getJoinedChallenges(userId: String): Result<List<com.ecogo.data.Challenge>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUserChallenges(userId)
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
     * 每日签到
     * POST /api/v1/checkin?userId={userId}
     * Note: Previous checkIn was mock, now using real endpoint if available or keeping logic consistent.
     * The ApiService defines performCheckIn. Updating repository to use it.
     */
    suspend fun checkIn(userId: String): Result<CheckInResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.performCheckIn(userId)
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
     * 获取签到状态
     */
    suspend fun getCheckInStatus(userId: String): Result<CheckInStatus> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getCheckInStatus(userId)
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
     * 获取签到历史记录
     */
    suspend fun getCheckInHistory(userId: String): Result<List<CheckIn>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getCheckInHistory(userId)
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
     * 获取今日目标进度
     */
    suspend fun getDailyGoal(userId: String): Result<DailyGoal> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getDailyGoal(userId)
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
     * 获取天气和空气质量
     */
    suspend fun getWeather(): Result<Weather> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getWeather()
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
     * 获取通知列表
     */
    suspend fun getNotifications(userId: String): Result<List<Notification>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getNotifications(userId)
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
     * 标记通知为已读
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.markNotificationAsRead(notificationId)
                if (response.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.message))
                }
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
                val response = api.getCarbonFootprint(userId, period)
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
     * 获取学院碳足迹总和 (SoC Score)
     */
    suspend fun getFacultyTotalCarbon(): Result<com.ecogo.data.FacultyCarbonData> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getFacultyTotalCarbon()
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
     * 获取好友列表
     */
    suspend fun getFriends(userId: String): Result<List<Friend>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getFriends(userId)
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
     * 获取好友动态
     */
    suspend fun getFriendActivities(userId: String): Result<List<FriendActivity>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getFriendActivities(userId)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
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
        productType: String,
        quantity: Int = 1
    ): Result<RedeemResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RedeemRequest(userId, productId, productType, quantity)
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
     * 获取活跃广告
     */
    suspend fun getAdvertisements(): Result<List<com.ecogo.data.Advertisement>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAdvertisements()
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

    // ==================== 挑战系统相关 ====================

    /**
     * 获取所有挑战（从后端API）
     */
    suspend fun getChallenges(): Result<List<com.ecogo.data.Challenge>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAllChallenges()
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
     * 根据ID获取挑战详情（从后端API）
     */
    suspend fun getChallengeById(id: String): Result<com.ecogo.data.Challenge> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getChallengeById(id)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Challenge not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 参加挑战（调用后端API）
     */
    suspend fun acceptChallenge(challengeId: String, userId: String): Result<com.ecogo.data.UserChallengeProgress> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.joinChallenge(challengeId, userId)
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
     * 获取用户在某挑战的进度（从后端API）
     */
    suspend fun getChallengeProgress(challengeId: String, userId: String): Result<com.ecogo.data.UserChallengeProgress> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getChallengeProgress(challengeId, userId)
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
     * 退出挑战（调用后端API）
     */
    suspend fun leaveChallenge(challengeId: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.leaveChallenge(challengeId, userId)
                if (response.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ==================== 行程/交通相关 ====================

    /**
     * 获取交通方式列表
     */
    suspend fun getTransportModes(): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getTransportModes()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ==================== 绿色点位相关 ====================

    /**
     * 获取所有绿色点位
     */
    suspend fun getGreenSpots(): Result<List<com.ecogo.data.GreenSpot>> =
        withContext(Dispatchers.IO) {
            try {
                // TODO: 实际应调用真实API
                Result.success(MockData.GREEN_SPOTS)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 收集绿色点位
     */
    suspend fun collectSpot(spotId: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // TODO: 实际应调用真实API
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}