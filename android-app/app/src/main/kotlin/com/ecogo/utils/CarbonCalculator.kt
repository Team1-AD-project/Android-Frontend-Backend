package com.ecogo.utils

import com.ecogo.data.TransportMode
import kotlin.math.roundToInt

/**
 * 碳排放计算器
 * 根据交通方式和距离计算碳排放和节省
 */
object CarbonCalculator {
    
    // 碳排放系数 (g CO2/km)
    private val CARBON_RATES = mapOf(
        TransportMode.WALK to 0.0,
        TransportMode.CYCLE to 0.0,
        TransportMode.BUS to 50.0,
        TransportMode.MIXED to 30.0  // 平均值
    )
    
    // 基准：开车的碳排放 (g CO2/km)
    private const val CAR_CARBON_RATE = 120.0
    
    // 积分转换率：每节省1g CO2 = 多少积分
    private const val POINTS_PER_GRAM = 0.5
    
    /**
     * 计算碳排放
     * @param mode 交通方式
     * @param distanceKm 距离（千米）
     * @return 碳排放量（克）
     */
    fun calculateEmission(mode: TransportMode, distanceKm: Double): Double {
        val rate = CARBON_RATES[mode] ?: 0.0
        return (rate * distanceKm).roundToInt().toDouble()
    }
    
    /**
     * 计算节省的碳排放（与开车相比）
     * @param mode 交通方式
     * @param distanceKm 距离（千米）
     * @return 节省的碳排放量（克）
     */
    fun calculateSavings(mode: TransportMode, distanceKm: Double): Double {
        val carEmission = CAR_CARBON_RATE * distanceKm
        val currentEmission = calculateEmission(mode, distanceKm)
        return (carEmission - currentEmission).coerceAtLeast(0.0).roundToInt().toDouble()
    }
    
    /**
     * 根据节省的碳排放计算绿色积分
     * @param carbonSavedGrams 节省的碳排放（克）
     * @return 绿色积分
     */
    fun calculatePoints(carbonSavedGrams: Double): Int {
        return (carbonSavedGrams * POINTS_PER_GRAM).roundToInt()
    }
    
    /**
     * 计算节省的金额（假设替代出租车）
     * @param distanceKm 距离（千米）
     * @return 节省的金额（新元）
     */
    fun calculateMoneySaved(distanceKm: Double): Double {
        // 新加坡出租车费用：起步费3.9新元 + 每400米0.22新元
        val taxiFare = 3.9 + (distanceKm * 1000 / 400) * 0.22
        // 步行/骑行/公交的成本（公交约1新元）
        val costBus = 1.0
        return (taxiFare - costBus).coerceAtLeast(0.0)
    }
    
    /**
     * 格式化碳排放显示
     * @param grams 碳排放（克）
     * @return 格式化字符串
     */
    fun formatCarbon(grams: Double): String {
        return when {
            grams >= 1000 -> String.format("%.1fkg CO₂", grams / 1000)
            grams > 0 -> String.format("%.0fg CO₂", grams)
            else -> "0g CO₂"
        }
    }
    
    /**
     * 获取环保等级
     * @param carbonSaved 节省的碳排放（克）
     * @return 环保等级描述
     */
    fun getEcoRating(carbonSaved: Double): String {
        return when {
            carbonSaved >= 200 -> "🌟 超级环保"
            carbonSaved >= 100 -> "🌿 非常环保"
            carbonSaved >= 50 -> "🍃 环保"
            carbonSaved > 0 -> "♻️ 低碳"
            else -> "🚗 普通"
        }
    }
}
