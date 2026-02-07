package com.ecogo.app.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * 绿色出行统计工具类
 * 用于跟踪和管理用户的累计环保数据
 */
object GreenTravelStats {

    private const val PREFS_NAME = "green_travel_stats"
    private const val KEY_TOTAL_TRIPS = "total_trips"
    private const val KEY_TOTAL_CARBON_SAVED = "total_carbon_saved"
    private const val KEY_WEEKLY_TRIPS = "weekly_trips"
    private const val KEY_WEEKLY_CARBON_SAVED = "weekly_carbon_saved"
    private const val KEY_LAST_RESET_DATE = "last_reset_date"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 记录一次绿色出行
     */
    fun recordGreenTrip(context: Context, carbonSaved: Double) {
        val prefs = getPrefs(context)
        checkAndResetWeekly(prefs)

        prefs.edit().apply {
            putInt(KEY_TOTAL_TRIPS, prefs.getInt(KEY_TOTAL_TRIPS, 0) + 1)
            putFloat(KEY_TOTAL_CARBON_SAVED, prefs.getFloat(KEY_TOTAL_CARBON_SAVED, 0f) + carbonSaved.toFloat())
            putInt(KEY_WEEKLY_TRIPS, prefs.getInt(KEY_WEEKLY_TRIPS, 0) + 1)
            putFloat(KEY_WEEKLY_CARBON_SAVED, prefs.getFloat(KEY_WEEKLY_CARBON_SAVED, 0f) + carbonSaved.toFloat())
            apply()
        }
    }

    /**
     * 获取本周统计
     */
    fun getWeeklyStats(context: Context): Pair<Int, Double> {
        val prefs = getPrefs(context)
        checkAndResetWeekly(prefs)

        val trips = prefs.getInt(KEY_WEEKLY_TRIPS, 0)
        val carbonSaved = prefs.getFloat(KEY_WEEKLY_CARBON_SAVED, 0f).toDouble()
        return Pair(trips, carbonSaved)
    }

    /**
     * 获取总计统计
     */
    fun getTotalStats(context: Context): Pair<Int, Double> {
        val prefs = getPrefs(context)
        val trips = prefs.getInt(KEY_TOTAL_TRIPS, 0)
        val carbonSaved = prefs.getFloat(KEY_TOTAL_CARBON_SAVED, 0f).toDouble()
        return Pair(trips, carbonSaved)
    }

    /**
     * 计算相当于种植多少棵树
     * 假设一棵树一年吸收约 22 kg CO2
     */
    fun calculateTreeEquivalent(carbonSaved: Double): Double {
        return carbonSaved / 22.0
    }

    /**
     * 格式化累计影响文本
     */
    fun formatWeeklyImpact(context: Context): String {
        val (trips, carbonSaved) = getWeeklyStats(context)

        return if (trips > 0) {
            val treeEquiv = calculateTreeEquivalent(carbonSaved)
            if (treeEquiv >= 0.1) {
                String.format("🌱 本周绿色出行 %d 次，累计减碳 %.2f kg (≈ %.1f 棵树)", trips, carbonSaved, treeEquiv)
            } else {
                String.format("🌱 本周绿色出行 %d 次，累计减碳 %.2f kg", trips, carbonSaved)
            }
        } else {
            "🌱 开始您的第一次绿色出行吧"
        }
    }

    /**
     * 检查并重置每周统计（如果已经过了一周）
     */
    private fun checkAndResetWeekly(prefs: SharedPreferences) {
        val lastResetDate = prefs.getLong(KEY_LAST_RESET_DATE, 0)
        val currentDate = System.currentTimeMillis()

        // 获取上次重置和当前日期的周数
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = lastResetDate
        val lastWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val lastYear = calendar.get(Calendar.YEAR)

        // 如果跨周或跨年，重置每周统计
        if (currentYear != lastYear || currentWeek != lastWeek) {
            prefs.edit().apply {
                putInt(KEY_WEEKLY_TRIPS, 0)
                putFloat(KEY_WEEKLY_CARBON_SAVED, 0f)
                putLong(KEY_LAST_RESET_DATE, currentDate)
                apply()
            }
        } else if (lastResetDate == 0L) {
            // 首次使用，设置初始日期
            prefs.edit().putLong(KEY_LAST_RESET_DATE, currentDate).apply()
        }
    }
}
