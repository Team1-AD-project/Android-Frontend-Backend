package com.ecogo.api

/**
 * API 统一响应格式
 * 对应后端的 ResponseMessage<T>
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    val success: Boolean
        get() = code == 200 || code == 0
}

/**
 * 空响应（没有数据体）
 */
data class EmptyResponse(
    val message: String = "Success"
)
