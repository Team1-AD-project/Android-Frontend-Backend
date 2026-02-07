package com.ecogo.app.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * 地图工具类
 */
object MapUtils {

    /**
     * 计算两点之间的距离 (米)
     * 使用 Haversine 公式
     */
    fun calculateDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0 // 地球半径 (米)

        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val deltaLat = Math.toRadians(end.latitude - start.latitude)
        val deltaLng = Math.toRadians(end.longitude - start.longitude)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * 计算路线总距离 (米)
     */
    fun calculateTotalDistance(points: List<LatLng>): Double {
        if (points.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += calculateDistance(points[i], points[i + 1])
        }
        return totalDistance
    }

    /**
     * 米转换为公里
     */
    fun metersToKilometers(meters: Double): Double {
        return meters / 1000.0
    }

    /**
     * 格式化距离显示
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} 米"
        } else {
            String.format("%.2f 公里", meters / 1000)
        }
    }

    /**
     * 估算碳排放 (kg)
     * 基于交通方式和距离
     */
    fun estimateCarbonEmission(distanceKm: Double, transportMode: String): Double {
        // 碳排放因子 (kg CO2/km)
        val carbonFactor = when (transportMode) {
            "walking" -> 0.0
            "cycling" -> 0.0
            "bus" -> 0.089
            "subway" -> 0.041
            "driving" -> 0.21
            else -> 0.0
        }
        return distanceKm * carbonFactor
    }

    /**
     * 计算碳减排 (相比驾车)
     */
    fun calculateCarbonSaved(distanceKm: Double, transportMode: String): Double {
        val drivingEmission = estimateCarbonEmission(distanceKm, "driving")
        val actualEmission = estimateCarbonEmission(distanceKm, transportMode)
        return drivingEmission - actualEmission
    }

    /**
     * Location 转 LatLng
     */
    fun locationToLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }
}
