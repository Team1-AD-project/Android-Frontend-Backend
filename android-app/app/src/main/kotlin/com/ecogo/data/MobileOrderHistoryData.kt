package com.ecogo.data

import com.ecogo.api.PaginationDto
import com.google.gson.annotations.SerializedName

/**
 * 对齐后端 OrderController 返回的 data:
 * {
 *   "orders": [ { ... } ],
 *   "pagination": { ... }
 * }
 *
 * 同时兼容你旧写法如果 data 里叫 data: [ ... ]
 */
data class MobileOrderHistoryData(
    @SerializedName(value = "orders", alternate = ["data"])
    val orders: List<OrderSummaryUi> = emptyList(),
    val pagination: PaginationDto? = null
)

data class OrderSummaryUi(
    val id: String,
    val orderNumber: String? = null,
    val status: String? = null,
    val finalAmount: Double? = null,
    val createdAt: String? = null,
    val itemCount: Int? = null,
    val isRedemption: Boolean? = null,
    val trackingNumber: String? = null,
    val carrier: String? = null
)
