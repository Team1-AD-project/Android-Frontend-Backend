package com.ecogo.data

/**
 * 成就/徽章数据模型
 * 用于显示用户的成就和徽章收集进度
 */
data class Achievement(
    val id: String,              // 成就ID (如 "a1", "a2", "badge_001" 等)
    val name: String,            // 成就名称
    val description: String,     // 成就描述
    val unlocked: Boolean,       // 是否已解锁
    val howToUnlock: String      // 如何解锁的说明
)
