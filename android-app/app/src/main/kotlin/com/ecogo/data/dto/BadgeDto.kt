package com.ecogo.data.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Badge - 统一的徽章/服饰数据模型
 * 后端使用同一个表存储 badge 和 cloth
 */
data class BadgeDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("badgeId")
    val badgeId: String,

    @SerializedName("name")
    val name: Map<String, String>?,

    @SerializedName("description")
    val description: Map<String, String>?,

    @SerializedName("purchaseCost")
    val purchaseCost: Int?,

    @SerializedName("category")
    val category: String?,  // "badge" 或 "cloth"

    @SerializedName("subCategory")
    val subCategory: String?,  // cloth: head/face/body, badge: rank

    @SerializedName("acquisitionMethod")
    val acquisitionMethod: String?,

    @SerializedName("carbonThreshold")
    val carbonThreshold: Double?,

    @SerializedName("icon")
    val icon: BadgeIcon?,

    @SerializedName("isActive")
    val isActive: Boolean?,

    @SerializedName("createdAt")
    val createdAt: Date? = null
) {
    data class BadgeIcon(
        @SerializedName("url")
        val url: String?,

        @SerializedName("colorScheme")
        val colorScheme: String?
    )

    fun isCloth(): Boolean = category == "cloth"
    fun isBadge(): Boolean = category == "badge"
    fun getEnglishName(): String = name?.get("en") ?: "Unknown"
}

/**
 * UserBadge - 用户持有的徽章/服饰
 */
data class UserBadgeDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("badgeId")
    val badgeId: String,

    @SerializedName("unlockedAt")
    val unlockedAt: Date?,

    @SerializedName("isDisplay")
    val isDisplay: Boolean,

    @SerializedName("createdAt")
    val createdAt: Date? = null
)

/**
 * 购买请求
 */
data class PurchaseRequest(
    @SerializedName("user_id")
    val userId: String
)

/**
 * 切换佩戴请求
 */
data class ToggleDisplayRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("is_display")
    val isDisplay: Boolean
)

/**
 * 用户装备信息
 */
data class UserOutfitDto(
    val head: String? = null,
    val face: String? = null,
    val body: String? = null,
    val badge: String? = null
)