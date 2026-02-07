package com.ecogo.app.data.model

/**
 * 通用 API 响应包装类
 * 对应后端统一返回格式: {"code": number, "msg": "string", "data": T}
 */
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T?
) {
    val isSuccess: Boolean
        get() = code == 200

    // 兼容别名 - TripRepository等地方使用
    val success: Boolean
        get() = isSuccess

    val message: String
        get() = msg
}
