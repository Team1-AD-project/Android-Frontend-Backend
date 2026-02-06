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
    private var applicationContext: Context? = null
    
    /**
     * 初始化 TokenManager
     * 应在 Application 类中调用
     */
    fun init(context: Context) {
        applicationContext = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private const val KEY_VIP_ACTIVE = "vip_active"

    fun saveToken(token: String, userId: String, username: String, vipActive: Boolean) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_VIP_ACTIVE, vipActive)
            apply()
        }
    }

    fun isVipActive(): Boolean =
        prefs.getBoolean(KEY_VIP_ACTIVE, false)

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
     * 并跳转到登录页面
     */
    fun logout() {
        prefs.edit().clear().apply()
        
        applicationContext?.let { context ->
            // Use reflection or hardcoded class name to avoid circular dependency if needed, 
            // but com.ecogo.MainActivity is in the same module.
            try {
                // Assuming MainActivity is the host for the navigation graph
                val packageName = context.packageName
                val intent = android.content.Intent(context, Class.forName("$packageName.MainActivity"))
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                // Pass a generic "Force Login" extra if we want to handle it specifically, 
                // but clearing task naturally resets to start destination (LoginFragment)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 获取 Authorization Header
     */
    fun getAuthHeader(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
}
