package com.ecogo.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 地点类型枚举
 */
enum class LocationType {
    FACULTY,      // 学院
    CANTEEN,      // 食堂
    LIBRARY,      // 图书馆
    RESIDENCE,    // 宿舍
    FACILITY,     // 设施
    BUS_STOP,     // 公交站
    OTHER         // 其他
}

/**
 * 交通方式枚举
 */
enum class TransportMode {
    WALK,         // 步行
    CYCLE,        // 骑行
    BUS,          // 公交
    MIXED         // 混合
}

/**
 * 行程状态枚举
 */
enum class TripStatus {
    PLANNING,     // 规划中
    ACTIVE,       // 进行中
    PAUSED,       // 已暂停
    COMPLETED,    // 已完成
    CANCELLED     // 已取消
}

/**
 * 地点模型
 */
@Parcelize
data class NavLocation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType,
    val icon: String,
    val isFavorite: Boolean = false,
    val visitCount: Int = 0,
    val lastVisitTime: Long = 0
) : Parcelable

/**
 * 路线步骤
 */
@Parcelize
data class RouteStep(
    val instruction: String,
    val distance: Double,        // 米
    val duration: Int,            // 秒
    val mode: TransportMode,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val polyline: String = ""
) : Parcelable

/**
 * 路线模型
 */
@Parcelize
data class NavRoute(
    val id: String,
    val origin: NavLocation,
    val destination: NavLocation,
    val mode: TransportMode,
    val distance: Double,         // 千米
    val duration: Int,            // 分钟
    val carbonEmission: Double,   // g CO2
    val carbonSaved: Double,      // g CO2
    val points: Int,              // 绿色积分
    val steps: List<RouteStep>,
    val polyline: String,
    val isRecommended: Boolean = false,
    val badge: String = ""        // 标签：最环保、最快等
) : Parcelable

/**
 * 行程记录
 */
@Parcelize
data class Trip(
    val id: String,
    val route: NavRoute,
    val startTime: Long,
    val endTime: Long? = null,
    val status: TripStatus,
    val actualDistance: Double = 0.0,
    val actualCarbonSaved: Double = 0.0,
    val pointsEarned: Int = 0,
    val achievementUnlocked: String? = null
) : Parcelable

/**
 * 公交车信息
 */
@Parcelize
data class BusInfo(
    val busId: String,
    val routeName: String,
    val destination: String,
    val currentLat: Double,
    val currentLng: Double,
    val etaMinutes: Int,
    val stopsAway: Int,
    val crowdLevel: String,      // 低、中、高
    val plateNumber: String,
    val status: String,           // arriving, coming, delayed
    val color: String = "#DB2777"
) : Parcelable

/**
 * 地图设置
 */
@Parcelize
data class MapSettings(
    val preferEcoRoute: Boolean = true,
    val avoidStairs: Boolean = false,
    val preferIndoor: Boolean = true,
    val showBusStops: Boolean = true,
    val showBikePaths: Boolean = true,
    val showGreenRoutes: Boolean = true,
    val showCrowdData: Boolean = false,
    val show3DBuildings: Boolean = false,
    val showTraffic: Boolean = true
) : Parcelable

/**
 * 导航状态
 */
enum class NavigationState {
    IDLE,         // 空闲
    SEARCHING,    // 搜索中
    PLANNING,     // 规划中
    NAVIGATING,   // 导航中
    COMPLETED     // 已完成
}

/**
 * 搜索历史
 */
@Parcelize
data class SearchHistory(
    val query: String,
    val location: NavLocation?,
    val timestamp: Long
) : Parcelable

/**
 * 路线选项对比
 */
@Parcelize
data class RouteOption(
    val route: NavRoute,
    val carbonComparison: Double,  // 与开车相比节省的碳排放
    val moneySaved: Double,         // 节省的金额
    val healthBenefit: String       // 健康益处描述
) : Parcelable
