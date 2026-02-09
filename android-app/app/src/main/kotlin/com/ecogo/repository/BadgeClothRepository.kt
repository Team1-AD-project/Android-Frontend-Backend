package com.ecogo.repository

import android.util.Log
import com.ecogo.api.BadgeApiService
import com.ecogo.data.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Badge & Cloth Repository
 * 统一管理 badge 和 cloth 的数据交互
 */
class BadgeClothRepository(
    private val apiService: BadgeApiService
) {

    companion object {
        private const val TAG = "BadgeClothRepository"
    }

    // ==================== 商店相关 ====================

    /**
     * 获取商店所有物品
     */
    suspend fun getShopList(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShopList()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load shop"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading shop", e)
            Result.failure(e)
        }
    }

    /**
     * 获取服饰商店列表
     */
    suspend fun getClothShop(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShopList()
            if (response.success && response.data != null) {
                val cloths = response.data.filter { it.category == "cloth" }
                Result.success(cloths)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load cloth shop"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cloth shop", e)
            Result.failure(e)
        }
    }

    /**
     * 获取徽章商店列表
     */
    suspend fun getBadgeShop(): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShopList()
            if (response.success && response.data != null) {
                val badges = response.data.filter { it.category == "badge" }
                Result.success(badges)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load badge shop"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading badge shop", e)
            Result.failure(e)
        }
    }

    // ==================== 用户背包相关 ====================

    /**
     * 获取用户所有物品
     */
    suspend fun getMyItems(userId: String): Result<List<UserBadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyItems(userId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load items"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user items", e)
            Result.failure(e)
        }
    }

    /**
     * 获取用户的服饰（只要 cloth）
     */
    suspend fun getMyCloths(userId: String): Result<List<UserBadgeDto>> = withContext(Dispatchers.IO) {
        try {
            // 1. 获取用户所有物品
            val userItemsResult = getMyItems(userId)
            if (userItemsResult.isFailure) {
                return@withContext userItemsResult
            }

            val userItems = userItemsResult.getOrNull() ?: emptyList()

            // 2. 获取商店列表判断 category
            val shopResult = getShopList()
            if (shopResult.isFailure) {
                return@withContext Result.success(emptyList())
            }

            val shopItems = shopResult.getOrNull() ?: emptyList()
            val clothBadgeIds = shopItems.filter { it.category == "cloth" }.map { it.badgeId }.toSet()

            // 3. 过滤出服饰
            val cloths = userItems.filter { it.badgeId in clothBadgeIds }
            Result.success(cloths)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user cloths", e)
            Result.failure(e)
        }
    }

    /**
     * 获取用户的徽章（只要 badge）
     */
    suspend fun getMyBadges(userId: String): Result<List<UserBadgeDto>> = withContext(Dispatchers.IO) {
        try {
            // 1. 获取用户所有物品
            val userItemsResult = getMyItems(userId)
            if (userItemsResult.isFailure) {
                return@withContext userItemsResult
            }

            val userItems = userItemsResult.getOrNull() ?: emptyList()

            // 2. 获取商店列表判断 category
            val shopResult = getShopList()
            if (shopResult.isFailure) {
                return@withContext Result.success(emptyList())
            }

            val shopItems = shopResult.getOrNull() ?: emptyList()
            val badgeBadgeIds = shopItems.filter { it.category == "badge" }.map { it.badgeId }.toSet()

            // 3. 过滤出徽章
            val badges = userItems.filter { it.badgeId in badgeBadgeIds }
            Result.success(badges)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user badges", e)
            Result.failure(e)
        }
    }

    // ==================== 购买相关 ====================

    /**
     * 购买物品（badge 或 cloth 通用）
     */
    suspend fun purchaseItem(
        userId: String,
        badgeId: String
    ): Result<UserBadgeDto> = withContext(Dispatchers.IO) {
        try {
            val request = PurchaseRequest(userId)
            val response = apiService.purchaseItem(badgeId, request)
            if (response.success && response.data != null) {
                Log.d(TAG, "Purchased item: $badgeId")
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to purchase"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error purchasing item: $badgeId", e)
            Result.failure(e)
        }
    }

    // ==================== 佩戴相关 ====================

    /**
     * 佩戴物品
     *
     * 后端会自动处理互斥：
     * - badge (rank): 同类互斥
     * - cloth: 按 subCategory 互斥
     */
    suspend fun equipItem(
        userId: String,
        badgeId: String
    ): Result<UserBadgeDto> = withContext(Dispatchers.IO) {
        try {
            val request = ToggleDisplayRequest(userId, true)
            val response = apiService.toggleDisplay(badgeId, request)
            if (response.success && response.data != null) {
                Log.d(TAG, "Equipped item: $badgeId")
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to equip"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error equipping item: $badgeId", e)
            Result.failure(e)
        }
    }

    /**
     * 卸下物品
     */
    suspend fun unequipItem(
        userId: String,
        badgeId: String
    ): Result<UserBadgeDto> = withContext(Dispatchers.IO) {
        try {
            val request = ToggleDisplayRequest(userId, false)
            val response = apiService.toggleDisplay(badgeId, request)
            if (response.success && response.data != null) {
                Log.d(TAG, "Unequipped item: $badgeId")
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to unequip"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unequipping item: $badgeId", e)
            Result.failure(e)
        }
    }

    // ==================== 查询相关 ====================

    /**
     * 按子分类查询
     * 可用于按部位获取服饰：head, face, body
     */
    suspend fun getItemsBySubCategory(subCategory: String): Result<List<BadgeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getItemsBySubCategory(subCategory)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load items"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading items by subCategory: $subCategory", e)
            Result.failure(e)
        }
    }

    /**
     * 获取用户当前装备
     * 从用户物品中提取 isDisplay=true 的物品
     */
    suspend fun getUserOutfit(userId: String): Result<UserOutfitDto> = withContext(Dispatchers.IO) {
        try {
            // 1. 获取用户所有物品
            val userItemsResult = getMyItems(userId)
            if (userItemsResult.isFailure) {
                return@withContext Result.success(UserOutfitDto())
            }

            val userItems = userItemsResult.getOrNull() ?: emptyList()
            val equipped = userItems.filter { it.isDisplay }

            // 2. 获取商店列表获取完整信息
            val shopResult = getShopList()
            if (shopResult.isFailure) {
                return@withContext Result.success(UserOutfitDto())
            }

            val shopItems = shopResult.getOrNull() ?: emptyList()
            val shopMap = shopItems.associateBy { it.badgeId }

            // 3. 组装装备信息
            val outfit = UserOutfitDto(
                head = equipped.find { shopMap[it.badgeId]?.subCategory == "head" }?.badgeId,
                face = equipped.find { shopMap[it.badgeId]?.subCategory == "face" }?.badgeId,
                body = equipped.find { shopMap[it.badgeId]?.subCategory == "body" }?.badgeId,
                badge = equipped.find { shopMap[it.badgeId]?.category == "badge" }?.badgeId
            )

            Log.d(TAG, "User outfit: $outfit")
            Result.success(outfit)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user outfit", e)
            Result.failure(e)
        }
    }
}