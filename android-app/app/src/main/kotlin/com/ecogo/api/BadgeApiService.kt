package com.ecogo.api

import com.ecogo.data.dto.BadgeDto
import com.ecogo.data.dto.UserBadgeDto
import com.ecogo.data.dto.PurchaseRequest
import com.ecogo.data.dto.ToggleDisplayRequest
import retrofit2.http.*

/**
 * Badge & Cloth API Service
 * 统一处理 badge 和 cloth 的 API 调用
 */
interface BadgeApiService {

    /**
     * 获取商店列表
     * GET /api/v1/mobile/badges/shop
     */
    @GET("api/v1/mobile/badges/shop")
    suspend fun getShopList(): ApiResponse<List<BadgeDto>>

    /**
     * 获取用户的背包
     * GET /api/v1/mobile/badges/user/{user_id}
     */
    @GET("api/v1/mobile/badges/user/{user_id}")
    suspend fun getMyItems(
        @Path("user_id") userId: String
    ): ApiResponse<List<UserBadgeDto>>

    /**
     * 购买物品
     * POST /api/v1/mobile/badges/{badge_id}/purchase
     */
    @POST("api/v1/mobile/badges/{badge_id}/purchase")
    suspend fun purchaseItem(
        @Path("badge_id") badgeId: String,
        @Body request: PurchaseRequest
    ): ApiResponse<UserBadgeDto>

    /**
     * 切换佩戴状态
     * PUT /api/v1/mobile/badges/{badge_id}/display
     */
    @PUT("api/v1/mobile/badges/{badge_id}/display")
    suspend fun toggleDisplay(
        @Path("badge_id") badgeId: String,
        @Body request: ToggleDisplayRequest
    ): ApiResponse<UserBadgeDto>

    /**
     * 按子分类查询
     * GET /api/v1/mobile/badges/sub-category/{sub_category}
     */
    @GET("api/v1/mobile/badges/sub-category/{sub_category}")
    suspend fun getItemsBySubCategory(
        @Path("sub_category") subCategory: String
    ): ApiResponse<List<BadgeDto>>
}