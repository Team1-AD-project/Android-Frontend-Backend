package com.ecogo.repository

import com.ecogo.api.ApiResponse
import com.ecogo.api.BadgeApiService
import com.ecogo.data.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Badge & Cloth Repository
 * 统一处理徽章和服饰的数据请求
 */
class BadgeClothRepository(
    private val apiService: BadgeApiService
) {

    // ========== 通用方法 ==========

    /**
     * 获取商店列表（包含 badge 和 cloth）
     */
    suspend fun getShopList(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShopList()
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load shop"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取用户背包（包含 badge 和 cloth）
     */
    suspend fun getMyItems(userId: String): Result<List<UserBadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyItems(userId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 购买物品（badge 或 cloth）
     */
    suspend fun purchaseItem(userId: String, badgeId: String): Result<UserBadgeDto> = withContext(Dispatchers.IO) {
        try {
            val request = PurchaseRequest(userId)
            val response = apiService.purchaseItem(badgeId, request)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Purchase failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 切换佩戴状态（badge 或 cloth）
     */
    suspend fun toggleDisplay(userId: String, badgeId: String, isDisplay: Boolean): Result<UserBadgeDto> = withContext(Dispatchers.IO) {
        try {
            val request = ToggleDisplayRequest(userId, isDisplay)
            val response = apiService.toggleDisplay(badgeId, request)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Toggle failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Badge 专用方法 ==========

    /**
     * 获取 Badge 列表（仅徽章）
     */
    suspend fun getBadgeList(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShopList()
            if (response.code == 200 && response.data != null) {
                val badges = response.data.filter { it.category == "badge" }
                Result.success(badges)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load badges"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取用户的 Badge 背包（仅徽章）
     */
    suspend fun getMyBadges(userId: String): Result<List<UserBadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyItems(userId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load badges"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Cloth 专用方法（保持不变）==========

    /**
     * 装备物品（Cloth）
     */
    suspend fun equipItem(userId: String, badgeId: String): Result<UserBadgeDto> {
        return toggleDisplay(userId, badgeId, true)
    }

    /**
     * 卸下物品（Cloth）
     */
    suspend fun unequipItem(userId: String, badgeId: String): Result<UserBadgeDto> {
        return toggleDisplay(userId, badgeId, false)
    }

    /**
     * 获取用户装备信息
     */
    suspend fun getUserOutfit(userId: String): Result<UserOutfitDto> = withContext(Dispatchers.IO) {
        try {
            val myItemsResult = getMyItems(userId)
            if (myItemsResult.isFailure) {
                return@withContext Result.failure(myItemsResult.exceptionOrNull()!!)
            }

            val userItems = myItemsResult.getOrNull() ?: emptyList()
            val displayedItems = userItems.filter { it.isDisplay }

            val shopResult = getShopList()
            if (shopResult.isFailure) {
                return@withContext Result.failure(shopResult.exceptionOrNull()!!)
            }

            val shopItems = shopResult.getOrNull() ?: emptyList()
            val shopMap = shopItems.associateBy { it.badgeId }

            var head: String? = null
            var face: String? = null
            var body: String? = null
            var badge: String? = null

            displayedItems.forEach { userItem ->
                val shopItem = shopMap[userItem.badgeId]
                when (shopItem?.subCategory) {
                    "head" -> head = userItem.badgeId
                    "face" -> face = userItem.badgeId
                    "body" -> body = userItem.badgeId
                    "rank" -> badge = userItem.badgeId // ⚠️ 注意：badge 的 subCategory 是 "rank"
                }
            }

            Result.success(UserOutfitDto(head, face, body, badge))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}