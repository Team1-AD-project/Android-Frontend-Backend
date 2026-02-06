package com.ecogo.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 积分管理器（单例）
 * 用于统一管理用户积分状态
 */
object PointsManager {
    
    private val _currentPoints = MutableStateFlow(0)
    val currentPoints: StateFlow<Int> = _currentPoints
    
    private val _totalEarned = MutableStateFlow(0)
    val totalEarned: StateFlow<Int> = _totalEarned
    
    private val _totalSpent = MutableStateFlow(0)
    val totalSpent: StateFlow<Int> = _totalSpent
    
    /**
     * 从服务器刷新积分数据
     */
    suspend fun refreshPoints() {
        // TODO: 实现真实的积分API调用
        // 目前使用模拟数据
        try {
            // 模拟从服务器获取积分
            _currentPoints.value = 880
            _totalEarned.value = 1500
            _totalSpent.value = 620
        } catch (e: Exception) {
            // 刷新失败时保持当前值
        }
    }
    
    /**
     * 本地更新积分值（用于 UI 即时反馈）
     * 注意：应该随后调用 refreshPoints() 同步真实数据
     */
    fun updatePointsLocally(points: Int) {
        _currentPoints.value = points
    }
    
    /**
     * 扣除积分（本地更新）
     */
    fun deductPoints(amount: Int) {
        _currentPoints.value = (_currentPoints.value - amount).coerceAtLeast(0)
    }
    
    /**
     * 增加积分（本地更新）
     */
    fun addPoints(amount: Int) {
        _currentPoints.value += amount
    }
}
