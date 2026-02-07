package com.ecogo.app.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * 路径简化工具
 * 用于减少路径点数量，优化数据传输和地图渲染性能
 *
 * 使用Douglas-Peucker算法简化路径
 */
object RouteSimplifier {

    /**
     * 简化路径点
     *
     * @param points 原始路径点列表
     * @param tolerance 容差值（米），值越大简化程度越高，推荐值：10-50米
     * @return 简化后的路径点列表
     *
     * 示例：
     * ```kotlin
     * val originalPoints = listOf(...)  // 1000个点
     * val simplifiedPoints = RouteSimplifier.simplify(originalPoints, tolerance = 20.0)
     * // simplifiedPoints 可能只有 50-100 个点
     * ```
     */
    fun simplify(points: List<LatLng>, tolerance: Double = 20.0): List<LatLng> {
        if (points.size <= 2) return points

        return douglasPeucker(points, tolerance)
    }

    /**
     * Douglas-Peucker 算法实现
     */
    private fun douglasPeucker(points: List<LatLng>, tolerance: Double): List<LatLng> {
        if (points.size <= 2) return points

        // 找到距离线段最远的点
        var maxDistance = 0.0
        var maxIndex = 0
        val end = points.size - 1

        for (i in 1 until end) {
            val distance = perpendicularDistance(points[i], points[0], points[end])
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        // 如果最大距离大于容差，递归简化
        val result = mutableListOf<LatLng>()
        if (maxDistance > tolerance) {
            // 递归简化左右两部分
            val leftPart = douglasPeucker(points.subList(0, maxIndex + 1), tolerance)
            val rightPart = douglasPeucker(points.subList(maxIndex, points.size), tolerance)

            // 合并结果（去除重复的中间点）
            result.addAll(leftPart.dropLast(1))
            result.addAll(rightPart)
        } else {
            // 距离小于容差，只保留首尾点
            result.add(points[0])
            result.add(points[end])
        }

        return result
    }

    /**
     * 计算点到线段的垂直距离（米）
     */
    private fun perpendicularDistance(point: LatLng, lineStart: LatLng, lineEnd: LatLng): Double {
        val x = point.latitude
        val y = point.longitude
        val x1 = lineStart.latitude
        val y1 = lineStart.longitude
        val x2 = lineEnd.latitude
        val y2 = lineEnd.longitude

        val A = x - x1
        val B = y - y1
        val C = x2 - x1
        val D = y2 - y1

        val dot = A * C + B * D
        val lenSq = C * C + D * D
        var param = -1.0

        if (lenSq != 0.0) {
            param = dot / lenSq
        }

        val xx: Double
        val yy: Double

        when {
            param < 0 -> {
                xx = x1
                yy = y1
            }
            param > 1 -> {
                xx = x2
                yy = y2
            }
            else -> {
                xx = x1 + param * C
                yy = y1 + param * D
            }
        }

        return calculateDistance(point, LatLng(xx, yy))
    }

    /**
     * 计算两点之间的距离（米）- Haversine公式
     */
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val R = 6371000.0 // 地球半径（米）
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val dLat = Math.toRadians(point2.latitude - point1.latitude)
        val dLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    /**
     * 按固定间隔抽样简化路径
     *
     * @param points 原始路径点列表
     * @param interval 抽样间隔（每隔N个点取一个）
     * @return 简化后的路径点列表
     *
     * 示例：
     * ```kotlin
     * // 每隔10个点取一个
     * val simplifiedPoints = RouteSimplifier.simplifyByInterval(points, interval = 10)
     * ```
     */
    fun simplifyByInterval(points: List<LatLng>, interval: Int): List<LatLng> {
        if (points.size <= 2) return points

        val result = mutableListOf<LatLng>()
        result.add(points.first()) // 保留起点

        for (i in interval until points.size step interval) {
            result.add(points[i])
        }

        // 确保终点被保留
        if (result.last() != points.last()) {
            result.add(points.last())
        }

        return result
    }

    /**
     * 按目标点数简化路径
     *
     * @param points 原始路径点列表
     * @param targetCount 目标点数
     * @return 简化后的路径点列表
     *
     * 示例：
     * ```kotlin
     * // 将路径简化为最多100个点
     * val simplifiedPoints = RouteSimplifier.simplifyToCount(points, targetCount = 100)
     * ```
     */
    fun simplifyToCount(points: List<LatLng>, targetCount: Int): List<LatLng> {
        if (points.size <= targetCount) return points

        val interval = points.size / targetCount
        return simplifyByInterval(points, interval.coerceAtLeast(1))
    }

    /**
     * 获取路径统计信息
     */
    fun getRouteStats(points: List<LatLng>): RouteStats {
        if (points.isEmpty()) {
            return RouteStats(0, 0.0, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        }

        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += calculateDistance(points[i], points[i + 1])
        }

        return RouteStats(
            pointCount = points.size,
            totalDistance = totalDistance,
            startPoint = points.first(),
            endPoint = points.last()
        )
    }
}

/**
 * 路径统计数据
 */
data class RouteStats(
    val pointCount: Int,           // 路径点数量
    val totalDistance: Double,     // 总距离（米）
    val startPoint: LatLng,        // 起点
    val endPoint: LatLng           // 终点
) {
    val totalDistanceKm: Double
        get() = totalDistance / 1000.0
}
