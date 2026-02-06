package com.ecogo

import android.app.Application
import com.ecogo.repository.EcoGoRepository

/**
 * EcoGo Application 类
 * 用于应用级别的初始化和单例管理
 */
class EcoGoApplication : Application() {
    
    companion object {
        // 全局单例 Repository，避免重复创建
        lateinit var repository: EcoGoRepository
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Repository 单例
        repository = EcoGoRepository()

        // Initialize TokenManager globally
        com.ecogo.auth.TokenManager.init(this)
        
        // 预加载关键数据（可选，在后台线程执行）
        // Thread {
        //     preloadCriticalData()
        // }.start()
    }
    
    /**
     * 预加载关键数据（可选）
     */
    private fun preloadCriticalData() {
        // 在这里可以预加载一些关键数据
        // 例如：用户配置、缓存等
    }
}
