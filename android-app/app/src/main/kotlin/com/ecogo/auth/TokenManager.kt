package com.ecogo.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Token 管理器
 * 用于管理用户认证 Token 和基本用户信息
 */
object TokenManager {
    private const val PREFS_NAME = "ecogo_auth"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    
    private lateinit var prefs: SharedPreferences
    
    /**
     * 初始化 TokenManager
     * 应在 Application 类中调用
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 保存 Token 和用户信息
     */
    fun saveToken(token: String, userId: String, username: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            apply()
        }
    }
    
    /**
     * 获取 Token
     */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    
    /**
     * 获取用户 ID
     */
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    /**
     * 获取用户名
     */
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean = getToken() != null
    
    /**
     * 清除 Token（登出）
     */
    fun clearToken() {
        prefs.edit().clear().apply()
    }
    
    /**
     * 获取 Authorization Header
     */
    fun getAuthHeader(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
}
