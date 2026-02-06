package com.ecogo.utils

/**
 * 地图工具类
 * 注意：地图功能临时禁用，需要配置 Google Maps SDK
 */
object MapUtils {
    
    /**
     * 格式化时长（秒转为分钟或小时）
     */
    fun formatDuration(seconds: Int): String {
        return when {
            seconds < 60 -> "${seconds}秒"
            seconds < 3600 -> "${seconds / 60}分钟"
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                if (minutes > 0) {
                    "${hours}小时${minutes}分钟"
                } else {
                    "${hours}小时"
                }
            }
        }
    }
    
    /**
     * 格式化距离（米转为米或公里）
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()}米"
        } else {
            String.format("%.1f公里", meters / 1000)
        }
    }
}
