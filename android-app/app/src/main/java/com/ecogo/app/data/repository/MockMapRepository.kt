package com.ecogo.app.data.repository

import android.util.Log
import com.ecogo.app.data.model.*
import com.ecogo.app.service.DirectionsService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mock 地图数据仓库
 *
 * 用途：在没有后端服务器的情况下，返回模拟数据用于测试和演示
 *
 * 使用方法：
 * 1. 在 MapViewModel 中将 MapRepository 替换为 MockMapRepository
 * 2. 或者使用依赖注入在调试模式下自动切换
 */
class MockMapRepository : IMapRepository {

    companion object {
        private const val TAG = "MockMapRepository"
    }

    // 模拟的行程ID计数器
    private var tripIdCounter = 1000

    // 当前模拟行程的数据
    private var currentTripStartTime: String? = null
    private var currentTripStartPoint: GeoPoint? = null

    // ========================================
    // 行程追踪相关
    // ========================================

    /**
     * 开始行程追踪 - Mock 实现
     *
     * 模拟行为：
     * 1. 延迟 500ms 模拟网络请求
     * 2. 生成一个唯一的 trip_id
     * 3. 返回成功状态
     */
    override suspend fun startTripTracking(
        userId: String,
        startPoint: GeoPoint,
        startLocation: LocationInfo?
    ): Result<TripTrackData> = withContext(Dispatchers.IO) {
        // 模拟网络延迟
        delay(500)

        // 保存起点信息，用于后续计算
        currentTripStartPoint = startPoint
        currentTripStartTime = getCurrentTimeString()

        // 生成模拟数据
        val mockData = TripTrackData(
            trip_id = "MOCK_TRIP_${tripIdCounter++}",
            status = "tracking",
            start_time = currentTripStartTime!!,
            message = "行程已开始记录"
        )

        Result.success(mockData)
    }

    /**
     * 取消行程追踪 - Mock 实现
     */
    override suspend fun cancelTripTracking(
        tripId: String,
        userId: String,
        reason: String?
    ): Result<TripCancelData> = withContext(Dispatchers.IO) {
        delay(300)

        // 清理当前行程数据
        currentTripStartTime = null
        currentTripStartPoint = null

        val mockData = TripCancelData(
            trip_id = tripId,
            status = "cancelled",
            cancel_time = getCurrentTimeString(),
            message = "行程已取消"
        )

        Result.success(mockData)
    }

    /**
     * 获取实时地图数据 - Mock 实现
     *
     * 返回模拟的轨迹点
     */
    override suspend fun getTripMap(
        tripId: String,
        userId: String
    ): Result<TripMapData> = withContext(Dispatchers.IO) {
        delay(200)

        // 生成模拟轨迹点（基于起点）- GeoPoint(lng, lat)
        val startPoint = currentTripStartPoint ?: GeoPoint(lng = 121.4737, lat = 31.2304)
        val trackPoints = generateMockTrackPoints(startPoint)

        val mockData = TripMapData(
            trip_id = tripId,
            track_points = trackPoints,
            current_distance = calculateMockDistance(trackPoints),
            duration_seconds = 600, // 10分钟
            status = "tracking"
        )

        Result.success(mockData)
    }

    /**
     * 保存行程 - Mock 实现
     */
    override suspend fun saveTrip(
        tripId: String,
        userId: String,
        endPoint: GeoPoint,
        endLocation: LocationInfo?,
        distance: Double,
        endTime: String
    ): Result<TripSaveData> = withContext(Dispatchers.IO) {
        delay(500)

        val mockData = TripSaveData(
            trip_id = tripId,
            status = "completed",
            total_distance = distance,
            duration_minutes = 15,
            message = "行程已保存"
        )

        // 清理当前行程数据
        currentTripStartTime = null
        currentTripStartPoint = null

        Result.success(mockData)
    }

    /**
     * 计算碳足迹 - Mock 实现
     *
     * 模拟碳足迹计算逻辑：
     * - 步行: 0 g/km
     * - 骑行: 0 g/km
     * - 公交: 50 g/km
     * - 地铁: 30 g/km
     * - 驾车: 150 g/km
     */
    override suspend fun calculateCarbon(
        tripId: String,
        transportModes: List<String>
    ): Result<CarbonCalculateData> = withContext(Dispatchers.IO) {
        delay(400)

        // 模拟不同交通方式的碳排放（单位：g/km）
        val carbonFactors = mapOf(
            "walking" to 0.0,
            "cycling" to 0.0,
            "bus" to 50.0,
            "subway" to 30.0,
            "driving" to 150.0
        )

        // 模拟 5 公里行程
        val mockDistance = 5.0

        // 计算总碳排放
        val totalCarbon = transportModes.sumOf { mode ->
            carbonFactors[mode] ?: 100.0
        } * mockDistance / transportModes.size

        // 计算碳减排（与驾车相比）
        val drivingCarbon = 150.0 * mockDistance
        val carbonSaved = (drivingCarbon - totalCarbon).coerceAtLeast(0.0)

        val mockData = CarbonCalculateData(
            trip_id = tripId,
            total_carbon_emission = totalCarbon / 1000, // 转换为 kg
            carbon_saved = carbonSaved / 1000, // 转换为 kg
            green_points = (carbonSaved / 10).toInt(), // 每减排10g得1积分
            transport_breakdown = transportModes.associateWith { mode ->
                (carbonFactors[mode] ?: 100.0) * mockDistance / transportModes.size / 1000
            }
        )

        Result.success(mockData)
    }

    // ========================================
    // 路线推荐相关
    // ========================================

    /**
     * 获取最低碳排路线 - Mock 实现
     *
     * 返回以步行/骑行/公交为主的低碳路线
     * 使用 Google Directions API 获取真实道路路线
     */
    override suspend fun getLowestCarbonRoute(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ): Result<RouteRecommendData> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting low carbon route from (${startPoint.lat}, ${startPoint.lng}) to (${endPoint.lat}, ${endPoint.lng})")

        // 使用 Google Directions API 获取真实路线（步行模式）
        val origin = LatLng(startPoint.lat, startPoint.lng)
        val destination = LatLng(endPoint.lat, endPoint.lng)

        val directionsResult = DirectionsService.getRoute(origin, destination, "walking")

        val routePoints: List<GeoPoint>
        val distance: Double
        val duration: Int

        if (directionsResult != null) {
            // 使用真实路线
            Log.d(TAG, "Got real route with ${directionsResult.points.size} points")
            routePoints = directionsResult.points.map { GeoPoint(lng = it.longitude, lat = it.latitude) }
            distance = directionsResult.distanceMeters / 1000.0  // 转换为公里
            duration = directionsResult.durationSeconds / 60     // 转换为分钟
        } else {
            // 回退到直线（API 失败时）
            Log.w(TAG, "Directions API failed, using fallback straight line")
            routePoints = generateRoutePoints(startPoint, endPoint)
            distance = calculateDistance(startPoint, endPoint)
            duration = (distance / 4 * 60).toInt()
        }

        // 低碳路线：优先步行+公交
        val mockData = RouteRecommendData(
            route_id = "LOW_CARBON_${System.currentTimeMillis()}",
            route_type = "low_carbon",
            total_distance = distance,
            estimated_duration = duration,
            total_carbon = distance * 0.02, // 低碳约 20g/km
            carbon_saved = distance * 0.13, // 比驾车节省约 130g/km
            route_segments = listOf(
                RouteSegment(
                    transport_mode = "walking",
                    distance = distance * 0.3,
                    duration = (duration * 0.3).toInt(),
                    carbon_emission = 0.0,
                    instructions = "步行至公交站"
                ),
                RouteSegment(
                    transport_mode = "bus",
                    distance = distance * 0.6,
                    duration = (duration * 0.5).toInt(),
                    carbon_emission = distance * 0.6 * 0.05,
                    instructions = "乘坐公交车"
                ),
                RouteSegment(
                    transport_mode = "walking",
                    distance = distance * 0.1,
                    duration = (duration * 0.2).toInt(),
                    carbon_emission = 0.0,
                    instructions = "步行至目的地"
                )
            ),
            route_points = routePoints
        )

        Result.success(mockData)
    }

    /**
     * 获取平衡路线 - Mock 实现
     *
     * 返回时间和碳排平衡的路线（可能包含打车段）
     * 使用 Google Directions API 获取真实道路路线
     */
    override suspend fun getBalancedRoute(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint
    ): Result<RouteRecommendData> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting balanced route from (${startPoint.lat}, ${startPoint.lng}) to (${endPoint.lat}, ${endPoint.lng})")

        // 使用 Google Directions API 获取真实路线（公交模式）
        val origin = LatLng(startPoint.lat, startPoint.lng)
        val destination = LatLng(endPoint.lat, endPoint.lng)

        val directionsResult = DirectionsService.getRoute(origin, destination, "transit")
            ?: DirectionsService.getRoute(origin, destination, "driving")  // 公交失败则用驾车

        val routePoints: List<GeoPoint>
        val distance: Double
        val duration: Int

        if (directionsResult != null) {
            // 使用真实路线
            Log.d(TAG, "Got real route with ${directionsResult.points.size} points")
            routePoints = directionsResult.points.map { GeoPoint(lng = it.longitude, lat = it.latitude) }
            distance = directionsResult.distanceMeters / 1000.0  // 转换为公里
            duration = directionsResult.durationSeconds / 60     // 转换为分钟
        } else {
            // 回退到直线（API 失败时）
            Log.w(TAG, "Directions API failed, using fallback straight line")
            routePoints = generateRoutePoints(startPoint, endPoint)
            distance = calculateDistance(startPoint, endPoint)
            duration = (distance / 15 * 60).toInt()
        }

        // 平衡路线：地铁为主，可能有打车
        val mockData = RouteRecommendData(
            route_id = "BALANCED_${System.currentTimeMillis()}",
            route_type = "balanced",
            total_distance = distance,
            estimated_duration = duration,
            total_carbon = distance * 0.06, // 约 60g/km
            carbon_saved = distance * 0.09, // 比驾车节省约 90g/km
            route_segments = listOf(
                RouteSegment(
                    transport_mode = "walking",
                    distance = distance * 0.1,
                    duration = (duration * 0.1).toInt(),
                    carbon_emission = 0.0,
                    instructions = "步行至地铁站"
                ),
                RouteSegment(
                    transport_mode = "subway",
                    distance = distance * 0.7,
                    duration = (duration * 0.6).toInt(),
                    carbon_emission = distance * 0.7 * 0.03,
                    instructions = "乘坐地铁"
                ),
                RouteSegment(
                    transport_mode = "driving",
                    distance = distance * 0.2,
                    duration = (duration * 0.3).toInt(),
                    carbon_emission = distance * 0.2 * 0.15,
                    instructions = "打车至目的地"
                )
            ),
            route_points = routePoints
        )

        Result.success(mockData)
    }

    /**
     * 根据交通方式获取路线 - Mock 实现
     */
    override suspend fun getRouteByTransportMode(
        userId: String,
        startPoint: GeoPoint,
        endPoint: GeoPoint,
        transportMode: TransportMode
    ): Result<RouteRecommendData> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting route for mode: ${transportMode.displayName}")

        // 映射到 Google Directions API mode
        val apiMode = when (transportMode) {
            TransportMode.DRIVING -> "driving"
            TransportMode.WALKING -> "walking"
            TransportMode.CYCLING -> "bicycling"
            TransportMode.BUS, TransportMode.SUBWAY -> "transit"
        }

        val origin = LatLng(startPoint.lat, startPoint.lng)
        val destination = LatLng(endPoint.lat, endPoint.lng)

        val routePoints: List<GeoPoint>
        val distance: Double
        val duration: Int
        val steps: List<RouteStep>
        val alternatives: List<RouteAlternative>?

        // 公交模式获取多条路线
        if (apiMode == "transit") {
            val allRoutes = DirectionsService.getRoutes(origin, destination, apiMode)

            // 只保留包含 TRANSIT 步骤的路线（过滤掉纯步行路线）
            val transitRoutes = allRoutes.filter { route ->
                route.steps.any { it.travel_mode == "TRANSIT" }
            }

            if (transitRoutes.isNotEmpty()) {
                // 使用第一条真正的公交路线作为默认路线
                val firstRoute = transitRoutes[0]
                routePoints = firstRoute.points.map { GeoPoint(lng = it.longitude, lat = it.latitude) }
                distance = firstRoute.distanceMeters / 1000.0
                duration = firstRoute.durationSeconds / 60
                steps = firstRoute.steps

                // 生成所有路线选项
                alternatives = transitRoutes.mapIndexed { index, route ->
                    val routeDist = route.distanceMeters / 1000.0
                    val routeDur = route.durationSeconds / 60
                    val routeCarbon = calculateCarbonForMode(routeDist, transportMode).totalCarbon

                    // 生成路线摘要
                    val summary = generateRouteSummary(route.steps)

                    RouteAlternative(
                        index = index,
                        total_distance = routeDist,
                        estimated_duration = routeDur,
                        total_carbon = routeCarbon,
                        route_points = route.points.map { GeoPoint(lng = it.longitude, lat = it.latitude) },
                        route_steps = route.steps,
                        summary = summary
                    )
                }

                Log.d(TAG, "Found ${alternatives.size} transit routes (filtered from ${allRoutes.size} total routes)")
            } else {
                // Fallback 到步行
                Log.w(TAG, "Transit mode failed, falling back to walking")
                val walkingRoute = DirectionsService.getRoute(origin, destination, "walking")
                if (walkingRoute != null) {
                    routePoints = walkingRoute.points.map { GeoPoint(lng = it.longitude, lat = it.latitude) }
                    distance = walkingRoute.distanceMeters / 1000.0
                    duration = walkingRoute.durationSeconds / 60
                    steps = walkingRoute.steps
                    alternatives = null
                } else {
                    routePoints = generateRoutePoints(startPoint, endPoint)
                    distance = calculateDistance(startPoint, endPoint)
                    duration = estimateDuration(distance, transportMode)
                    steps = emptyList()
                    alternatives = null
                }
            }
        } else {
            // 非公交模式：单条路线
            val directionsResult = DirectionsService.getRoute(origin, destination, apiMode)

            if (directionsResult != null) {
                routePoints = directionsResult.points.map { GeoPoint(lng = it.longitude, lat = it.latitude) }
                distance = directionsResult.distanceMeters / 1000.0
                duration = directionsResult.durationSeconds / 60
                steps = directionsResult.steps
                alternatives = null
            } else {
                // Fallback：直线
                Log.w(TAG, "Directions API failed, using straight line")
                routePoints = generateRoutePoints(startPoint, endPoint)
                distance = calculateDistance(startPoint, endPoint)
                duration = estimateDuration(distance, transportMode)
                steps = emptyList()
                alternatives = null
            }
        }

        // 计算碳排放
        val carbonData = calculateCarbonForMode(distance, transportMode)

        Result.success(RouteRecommendData(
            route_id = "${transportMode.value.uppercase()}_${System.currentTimeMillis()}",
            route_type = transportMode.value,
            total_distance = distance,
            estimated_duration = duration,
            total_carbon = carbonData.totalCarbon,
            carbon_saved = carbonData.carbonSaved,
            route_segments = emptyList(),
            route_points = routePoints,
            route_steps = steps,
            route_alternatives = alternatives  // 传递多条路线选项
        ))
    }

    // ========================================
    // 辅助方法
    // ========================================

    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /**
     * 计算两点之间的直线距离（单位：公里）
     * 使用 Haversine 公式
     */
    private fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val R = 6371.0 // 地球半径（公里）

        val lat1 = Math.toRadians(p1.lat)
        val lat2 = Math.toRadians(p2.lat)
        val deltaLat = Math.toRadians(p2.lat - p1.lat)
        val deltaLng = Math.toRadians(p2.lng - p1.lng)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    /**
     * 生成路线点（在起点和终点之间线性插值）
     */
    private fun generateRoutePoints(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()
        val steps = 10

        for (i in 0..steps) {
            val ratio = i.toDouble() / steps
            points.add(GeoPoint(
                lng = start.lng + (end.lng - start.lng) * ratio,
                lat = start.lat + (end.lat - start.lat) * ratio
            ))
        }

        return points
    }

    /**
     * 生成模拟轨迹点
     */
    private fun generateMockTrackPoints(start: GeoPoint): List<TrackPoint> {
        val points = mutableListOf<TrackPoint>()
        var currentLat = start.lat
        var currentLng = start.lng

        repeat(10) { i ->
            // 每个点略微偏移，模拟移动轨迹
            currentLat += 0.0001 * (1 + Math.random())
            currentLng += 0.0001 * (1 + Math.random())

            points.add(TrackPoint(
                latitude = currentLat,
                longitude = currentLng,
                timestamp = System.currentTimeMillis() - (10 - i) * 60000,
                speed = 4.0 + Math.random() * 2 // 4-6 km/h
            ))
        }

        return points
    }

    /**
     * 计算轨迹总距离
     */
    private fun calculateMockDistance(points: List<TrackPoint>): Double {
        if (points.size < 2) return 0.0

        var total = 0.0
        for (i in 1 until points.size) {
            val p1 = GeoPoint(lng = points[i-1].longitude, lat = points[i-1].latitude)
            val p2 = GeoPoint(lng = points[i].longitude, lat = points[i].latitude)
            total += calculateDistance(p1, p2)
        }

        return total
    }

    /**
     * 估算行程时间
     */
    private fun estimateDuration(distanceKm: Double, mode: TransportMode): Int {
        val speedKmh = when (mode) {
            TransportMode.WALKING -> 4.0
            TransportMode.CYCLING -> 15.0
            TransportMode.BUS -> 20.0
            TransportMode.SUBWAY -> 35.0
            TransportMode.DRIVING -> 40.0
        }
        return (distanceKm / speedKmh * 60).toInt()  // 转为分钟
    }

    /**
     * 碳排放数据
     */
    private data class CarbonData(val totalCarbon: Double, val carbonSaved: Double)

    /**
     * 生成路线摘要（例如："地铁1号线 → 公交46路"）
     */
    private fun generateRouteSummary(steps: List<RouteStep>): String {
        val transitSteps = steps.filter { it.travel_mode == "TRANSIT" && it.transit_details != null }

        if (transitSteps.isEmpty()) {
            return "步行路线"
        }

        val summary = transitSteps.mapNotNull { step ->
            step.transit_details?.let {
                it.line_short_name ?: it.line_name
            }
        }.joinToString(" → ")

        return summary.ifEmpty { "公交路线" }
    }

    /**
     * 计算碳排放
     */
    private fun calculateCarbonForMode(distanceKm: Double, mode: TransportMode): CarbonData {
        // 碳排放因子 (kg CO2 / km)
        val emissionFactor = when (mode) {
            TransportMode.WALKING -> 0.0
            TransportMode.CYCLING -> 0.0
            TransportMode.BUS -> 0.05      // 公交 50g/km
            TransportMode.SUBWAY -> 0.03   // 地铁 30g/km
            TransportMode.DRIVING -> 0.15  // 驾车 150g/km
        }

        val totalCarbon = distanceKm * emissionFactor
        val drivingCarbon = distanceKm * 0.15  // 与驾车对比
        val carbonSaved = (drivingCarbon - totalCarbon).coerceAtLeast(0.0)

        return CarbonData(totalCarbon, carbonSaved)
    }
}
