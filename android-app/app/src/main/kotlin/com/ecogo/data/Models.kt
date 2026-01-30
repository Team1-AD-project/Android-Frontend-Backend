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
    val owned: Boolean = false
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

// Mascot Outfit
data class Outfit(
    val head: String = "none",
    val face: String = "none",
    val body: String = "none"
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
