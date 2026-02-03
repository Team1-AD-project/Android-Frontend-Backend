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

// Ranking (Leaderboard data, matches backend Ranking.java)
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

// Activity (Matches backend Activity.java)
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
    NORMAL,      // Normal expression
    HAPPY,       // Happy (existing)
    SAD,         // Sad
    THINKING,    // Thinking
    WAVING,      // Waving
    CELEBRATING, // Celebrating
    SLEEPING,    // Sleeping
    CONFUSED     // Confused
}

// Mascot Size Presets
enum class MascotSize(val dp: Int) {
    SMALL(32),    // Small icon
    MEDIUM(48),   // Medium size (avatar)
    LARGE(120),   // Large size (Profile)
    XLARGE(200)   // Extra large size (popup display)
}

// Mascot Outfit
data class Outfit(
    val head: String = "none",
    val face: String = "none",
    val body: String = "none",
    val badge: String = "none"  // Added badge slot
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
    val recommendation: String // Travel recommendation
)

// Notification
data class Notification(
    val id: String,
    val type: String, // activity, bus_delay, achievement, system
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null // Target to navigate after click
)

// Carbon Footprint
data class CarbonFootprint(
    val userId: String,
    val period: String, // daily, weekly, monthly
    val co2Saved: Float, // kg
    val equivalentTrees: Int, // Equivalent to how many trees
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

// Shop Product (Unified product model)
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val type: String,  // "voucher" 或 "goods"
    val category: String,  // "food", "transport", "eco_product", "merchandise", "digital"
    
    // Dual pricing
    val pointsPrice: Int?,  // null means points not supported
    val cashPrice: Double?,  // null means cash not supported
    
    // Stock and availability
    val available: Boolean = true,
    val stock: Int? = null,
    
    // Additional info
    val imageUrl: String? = null,
    val brand: String? = null,
    val validUntil: String? = null,
    val tags: List<String> = emptyList()
)

// Redeem request
data class RedeemRequest(
    val userId: String,
    val productId: String,
    val productType: String,
    val quantity: Int = 1
)

// Order data
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

// Redeem response
data class RedeemResponse(
    val success: Boolean,
    val message: String,
    val order: OrderDto?,
    val remainingPoints: Int?
)

// Challenge - Challenge data model
data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // INDIVIDUAL, TEAM, FACULTY
    val target: Int,  // Target value (e.g. 10 trips)
    val current: Int = 0, // Current progress
    val reward: Int,  // Points reward
    val badge: String? = null, // Badge ID
    val startTime: String,
    val endTime: String,
    val participants: Int = 0,
    val topUsers: List<User> = emptyList(),
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, EXPIRED
    val icon: String = "🏆"
)

// User - Simplified user model (for challenge leaderboard)
data class User(
    val id: String,
    val username: String,
    val points: Int = 0,
    val avatar: String? = null
)

// FeedItem - Community feed data model
data class FeedItem(
    val id: String,
    val userId: String,
    val username: String,
    val type: String, // TRIP, ACHIEVEMENT, ACTIVITY, CHALLENGE
    val content: String,
    val timestamp: Long,
    val likes: Int = 0,
    val iconUrl: String? = null
)

// GreenSpot - Green spot data model
data class GreenSpot(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val type: String, // TREE, RECYCLE_BIN, PARK, LANDMARK
    val reward: Int,
    val description: String,
    val collected: Boolean = false
)
