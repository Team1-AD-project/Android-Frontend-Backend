package com.ecogo.app.data.repository

import android.content.Context
import com.ecogo.app.data.local.AppDatabase
import com.ecogo.app.data.local.entity.NavigationHistory
import com.ecogo.app.data.local.entity.NavigationHistorySummary
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导航历史记录仓库
 * 单例模式，提供全局唯一的实例
 *
 * 使用方法：
 * ```kotlin
 * // 初始化（在Application中）
 * NavigationHistoryRepository.initialize(context)
 *
 * // 获取实例
 * val repository = NavigationHistoryRepository.getInstance()
 *
 * // 保存导航记录
 * repository.saveNavigationHistory(...)
 *
 * // 获取所有记录
 * val histories = repository.getAllHistories()
 * ```
 */
class NavigationHistoryRepository private constructor(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao = database.navigationHistoryDao()

    /**
     * 保存导航历史记录
     *
     * @param tripId 行程ID（可选）
     * @param userId 用户ID（可选）
     * @param startTime 开始时间（时间戳）
     * @param endTime 结束时间（时间戳）
     * @param origin 起点坐标
     * @param originName 起点名称
     * @param destination 终点坐标
     * @param destinationName 终点名称
     * @param routePoints 规划的路线点列表
     * @param trackPoints 实际轨迹点列表
     * @param totalDistance 总距离（米）
     * @param traveledDistance 实际行进距离（米）
     * @param transportMode 交通方式
     * @param detectedMode AI检测的交通方式（可选）
     * @param totalCarbon 总碳排放（kg）
     * @param carbonSaved 减少的碳排放（kg）
     * @param isGreenTrip 是否为绿色出行
     * @param greenPoints 绿色积分
     * @param routeType 路线类型（可选）
     * @param notes 备注（可选）
     * @return 插入记录的ID
     */
    suspend fun saveNavigationHistory(
        tripId: String? = null,
        userId: String? = null,
        startTime: Long,
        endTime: Long,
        origin: LatLng,
        originName: String,
        destination: LatLng,
        destinationName: String,
        routePoints: List<LatLng>,
        trackPoints: List<LatLng>,
        totalDistance: Double,
        traveledDistance: Double,
        transportMode: String,
        detectedMode: String? = null,
        totalCarbon: Double,
        carbonSaved: Double,
        isGreenTrip: Boolean,
        greenPoints: Int = 0,
        routeType: String? = null,
        notes: String? = null
    ): Long {
        val history = NavigationHistory(
            tripId = tripId,
            userId = userId,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = ((endTime - startTime) / 1000).toInt(),
            originLat = origin.latitude,
            originLng = origin.longitude,
            originName = originName,
            destinationLat = destination.latitude,
            destinationLng = destination.longitude,
            destinationName = destinationName,
            routePoints = convertLatLngListToJson(routePoints),
            trackPoints = convertLatLngListToJson(trackPoints),
            totalDistance = totalDistance,
            traveledDistance = traveledDistance,
            transportMode = transportMode,
            detectedMode = detectedMode,
            totalCarbon = totalCarbon,
            carbonSaved = carbonSaved,
            isGreenTrip = isGreenTrip,
            greenPoints = greenPoints,
            routeType = routeType,
            notes = notes
        )
        return dao.insert(history)
    }

    /**
     * 获取所有导航历史记录
     */
    suspend fun getAllHistories(): List<NavigationHistory> {
        return dao.getAll()
    }

    /**
     * 获取所有导航历史记录（Flow，实时更新）
     */
    fun getAllHistoriesFlow(): Flow<List<NavigationHistory>> {
        return dao.getAllFlow()
    }

    /**
     * 获取简化版历史记录列表
     */
    suspend fun getAllSummaries(): List<NavigationHistorySummary> {
        return dao.getAllSummaries()
    }

    /**
     * 根据ID获取单条记录
     */
    suspend fun getHistoryById(id: Long): NavigationHistory? {
        return dao.getById(id)
    }

    /**
     * 获取最近的N条记录
     */
    suspend fun getRecentHistories(limit: Int = 10): List<NavigationHistory> {
        return dao.getRecent(limit)
    }

    /**
     * 根据时间范围查询
     * @param startTime 开始时间（时间戳）
     * @param endTime 结束时间（时间戳）
     */
    suspend fun getHistoriesByTimeRange(startTime: Long, endTime: Long): List<NavigationHistory> {
        return dao.getByTimeRange(startTime, endTime)
    }

    /**
     * 获取今天的历史记录
     */
    suspend fun getTodayHistories(): List<NavigationHistory> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return dao.getByTimeRange(startOfDay, endOfDay)
    }

    /**
     * 获取本周的历史记录
     */
    suspend fun getThisWeekHistories(): List<NavigationHistory> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = calendar.timeInMillis

        return dao.getByTimeRange(startOfWeek, endOfWeek)
    }

    /**
     * 根据交通方式查询
     */
    suspend fun getHistoriesByTransportMode(mode: String): List<NavigationHistory> {
        return dao.getByTransportMode(mode)
    }

    /**
     * 获取所有绿色出行记录
     */
    suspend fun getGreenTrips(): List<NavigationHistory> {
        return dao.getGreenTrips()
    }

    /**
     * 根据用户ID查询
     */
    suspend fun getHistoriesByUserId(userId: String): List<NavigationHistory> {
        return dao.getByUserId(userId)
    }

    /**
     * 搜索历史记录（根据起点或终点名称）
     */
    suspend fun searchHistories(keyword: String): List<NavigationHistory> {
        return dao.search(keyword)
    }

    /**
     * 更新历史记录
     */
    suspend fun updateHistory(history: NavigationHistory) {
        dao.update(history)
    }

    /**
     * 删除历史记录
     */
    suspend fun deleteHistory(history: NavigationHistory) {
        dao.delete(history)
    }

    /**
     * 根据ID删除记录
     */
    suspend fun deleteHistoryById(id: Long) {
        dao.deleteById(id)
    }

    /**
     * 清空所有历史记录
     */
    suspend fun deleteAllHistories() {
        dao.deleteAll()
    }

    /**
     * 统计数据
     */
    suspend fun getStatistics(): NavigationStatistics {
        val totalCount = dao.getCount()
        val greenTripCount = dao.getGreenTripCount()
        val totalDistance = dao.getTotalDistance() ?: 0.0
        val totalCarbonSaved = dao.getTotalCarbonSaved() ?: 0.0

        return NavigationStatistics(
            totalTrips = totalCount,
            greenTrips = greenTripCount,
            totalDistanceMeters = totalDistance,
            totalCarbonSavedKg = totalCarbonSaved
        )
    }

    /**
     * 将LatLng列表转换为JSON字符串（用于存储）
     */
    private fun convertLatLngListToJson(points: List<LatLng>): String {
        val simplified = points.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        return com.google.gson.Gson().toJson(simplified)
    }

    /**
     * 将JSON字符串转换为LatLng列表（用于读取）
     */
    fun parseLatLngListFromJson(json: String): List<LatLng> {
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Double>>>() {}.type
        val list: List<Map<String, Double>> = com.google.gson.Gson().fromJson(json, type)
        return list.map { LatLng(it["lat"] ?: 0.0, it["lng"] ?: 0.0) }
    }

    companion object {
        @Volatile
        private var INSTANCE: NavigationHistoryRepository? = null

        /**
         * 初始化仓库（建议在Application中调用）
         */
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = NavigationHistoryRepository(context)
                    }
                }
            }
        }

        /**
         * 获取仓库实例
         * @throws IllegalStateException 如果未先调用initialize()
         */
        fun getInstance(): NavigationHistoryRepository {
            return INSTANCE ?: throw IllegalStateException(
                "NavigationHistoryRepository must be initialized by calling initialize(context) first"
            )
        }

        /**
         * 检查是否已初始化
         */
        fun isInitialized(): Boolean {
            return INSTANCE != null
        }
    }
}

/**
 * 导航统计数据
 */
data class NavigationStatistics(
    val totalTrips: Int,               // 总行程数
    val greenTrips: Int,               // 绿色出行次数
    val totalDistanceMeters: Double,   // 总距离（米）
    val totalCarbonSavedKg: Double     // 总减碳量（kg）
) {
    /**
     * 总距离（公里）
     */
    val totalDistanceKm: Double
        get() = totalDistanceMeters / 1000.0

    /**
     * 绿色出行比例
     */
    val greenTripPercentage: Double
        get() = if (totalTrips > 0) (greenTrips.toDouble() / totalTrips * 100) else 0.0
}
