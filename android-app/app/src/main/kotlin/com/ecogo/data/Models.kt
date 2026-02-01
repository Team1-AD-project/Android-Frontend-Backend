package com.ecogo.data

// Bus Route
data class BusRoute(
    val id: String? = null,
    val name: String,
    val from: String = "",
    val to: String = "",
    val color: String? = null,
    val status: String? = null,
    val time: String? = null,
    val crowd: String? = null,
    val number: String = name,
    val nextArrival: Int = 0,
    val crowding: String = "",
    val operational: Boolean = true
)

// Community/Faculty
data class Community(
    val name: String,
    val points: Int,
    val change: Int
)

// Ranking（排行榜数据，匹配后端 Ranking.java）
data class Ranking(
    val id: String? = null,
    val period: String,
    val rank: Int,
    val userId: String,
    val nickname: String,
    val steps: Int,
    val isVip: Boolean = false
)

// Shop Item
data class ShopItem(
    val id: String,
    val name: String,
    val type: String, // head, face, body
    val cost: Int,
    val owned: Boolean = false,
    val equipped: Boolean = false
)

// Voucher
data class Voucher(
    val id: String,
    val name: String,
    val cost: Int,
    val description: String,
    val available: Boolean = true
)

// Walking Route
data class WalkingRoute(
    val id: Int,
    val title: String,
    val time: String,
    val distance: String,
    val calories: String,
    val tags: List<String>,
    val description: String
)

// Activity（匹配后端 Activity.java）
data class Activity(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val type: String = "ONLINE", // ONLINE, OFFLINE
    val status: String = "DRAFT", // DRAFT, PUBLISHED, ONGOING, ENDED
    val rewardCredits: Int = 0,
    val maxParticipants: Int? = null,
    val currentParticipants: Int = 0,
    val participantIds: List<String> = emptyList(),
    val startTime: String? = null,
    val endTime: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Achievement/Badge
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val unlocked: Boolean
)

// History Item
data class HistoryItem(
    val id: Int,
    val action: String,
    val time: String,
    val points: String,
    val type: String // earn, spend
)

// Mascot Emotion States
enum class MascotEmotion {
    NORMAL,      // 正常表情
    HAPPY,       // 开心（已有）
    SAD,         // 伤心
    THINKING,    // 思考
    WAVING,      // 挥手
    CELEBRATING, // 庆祝
    SLEEPING,    // 睡觉
    CONFUSED     // 困惑
}

// Mascot Size Presets
enum class MascotSize(val dp: Int) {
    SMALL(32),    // 小图标
    MEDIUM(48),   // 中等尺寸（头像）
    LARGE(120),   // 大尺寸（Profile）
    XLARGE(200)   // 超大尺寸（弹窗展示）
}

// Mascot Outfit
data class Outfit(
    val head: String = "none",
    val face: String = "none",
    val body: String = "none",
    val badge: String = "none"  // 新增徽章槽位
)

// Faculty for Map
data class Faculty(
    val id: String,
    val name: String,
    val score: Int,
    val rank: Int,
    val color: String? = null,
    val icon: String? = null,
    val x: Float = 0.0f,
    val y: Float = 0.0f
)

// Faculty Data for Signup (with outfit configuration)
data class FacultyData(
    val id: String,
    val name: String,
    val color: String,
    val slogan: String,
    val outfit: Outfit
)

// Recommendation
data class RecommendationRequest(
    val destination: String
)

data class RecommendationResponse(
    val text: String,
    val tag: String
)

// Chat
data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val reply: String
)

// Voucher Redeem
data class VoucherRedeemRequest(
    val userId: String,
    val voucherId: String
)

// Daily Check-in
data class CheckInStatus(
    val userId: String,
    val lastCheckInDate: String, // yyyy-MM-dd format
    val consecutiveDays: Int = 0,
    val totalCheckIns: Int = 0,
    val pointsEarned: Int = 0
)

data class CheckInResponse(
    val success: Boolean,
    val pointsEarned: Int,
    val consecutiveDays: Int,
    val message: String
)

data class CheckIn(
    val id: String,
    val userId: String,
    val checkInDate: String, // yyyy-MM-dd format
    val pointsEarned: Int,
    val consecutiveDays: Int,
    val timestamp: String
)

// Daily Goal
data class DailyGoal(
    val id: String,
    val userId: String,
    val date: String, // yyyy-MM-dd format
    val stepGoal: Int = 10000,
    val currentSteps: Int = 0,
    val tripGoal: Int = 3,
    val currentTrips: Int = 0,
    val co2SavedGoal: Float = 2.0f, // kg
    val currentCo2Saved: Float = 0f
)

// Weather & Air Quality
data class Weather(
    val location: String = "NUS",
    val temperature: Int, // Celsius
    val condition: String, // Sunny, Rainy, Cloudy, etc.
    val humidity: Int, // percentage
    val aqi: Int, // Air Quality Index
    val aqiLevel: String, // Good, Moderate, Unhealthy, etc.
    val recommendation: String // 出行建议
)

// Notification
data class Notification(
    val id: String,
    val type: String, // activity, bus_delay, achievement, system
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null // 点击后跳转的目标
)

// Carbon Footprint
data class CarbonFootprint(
    val userId: String,
    val period: String, // daily, weekly, monthly
    val co2Saved: Float, // kg
    val equivalentTrees: Int, // 相当于多少棵树
    val tripsByBus: Int,
    val tripsByWalking: Int,
    val tripsByBicycle: Int = 0
)

// Friend / Social
data class Friend(
    val userId: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val points: Int,
    val rank: Int,
    val faculty: String? = null
)

data class FriendActivity(
    val friendId: String,
    val friendName: String,
    val action: String, // joined_activity, earned_badge, etc.
    val timestamp: String,
    val details: String
)

// Shop Product (统一商品模型)
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val type: String,  // "voucher" 或 "goods"
    val category: String,  // "food", "transport", "eco_product", "merchandise", "digital"
    
    // 双重价格
    val pointsPrice: Int?,  // null表示不支持积分
    val cashPrice: Double?,  // null表示不支持现金
    
    // 库存和可用性
    val available: Boolean = true,
    val stock: Int? = null,
    
    // 额外信息
    val imageUrl: String? = null,
    val brand: String? = null,
    val validUntil: String? = null,
    val tags: List<String> = emptyList()
)

// 兑换请求
data class RedeemRequest(
    val userId: String,
    val productId: String,
    val productType: String,
    val quantity: Int = 1
)

// 订单数据
data class OrderDto(
    val id: String,
    val userId: String,
    val productId: String,
    val productName: String,
    val productType: String,
    val quantity: Int,
    val pointsUsed: Int?,
    val cashPaid: Double?,
    val status: String, // pending, completed, cancelled
    val createdAt: String,
    val updatedAt: String? = null
)

// 兑换响应
data class RedeemResponse(
    val success: Boolean,
    val message: String,
    val order: OrderDto?,
    val remainingPoints: Int?
)
