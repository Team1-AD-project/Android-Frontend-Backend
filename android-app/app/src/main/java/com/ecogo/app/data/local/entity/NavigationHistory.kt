package com.ecogo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 导航历史记录实体
 * 用于存储用户完成的导航路线数据
 */
@Entity(tableName = "navigation_history")
@TypeConverters(NavigationHistoryConverters::class)
data class NavigationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 行程基本信息
    val tripId: String? = null,                    // 行程ID（如果有后端记录）
    val userId: String? = null,                    // 用户ID
    val startTime: Long,                           // 开始时间（时间戳）
    val endTime: Long,                             // 结束时间（时间戳）
    val durationSeconds: Int,                      // 行程时长（秒）

    // 位置信息
    val originLat: Double,                         // 起点纬度
    val originLng: Double,                         // 起点经度
    val originName: String,                        // 起点名称
    val destinationLat: Double,                    // 终点纬度
    val destinationLng: Double,                    // 终点经度
    val destinationName: String,                   // 终点名称

    // 路线信息
    val routePoints: String,                       // 路线点列表（JSON格式）
    val trackPoints: String,                       // 实际轨迹点列表（JSON格式）
    val totalDistance: Double,                     // 总距离（米）
    val traveledDistance: Double,                  // 实际行进距离（米）

    // 交通方式
    val transportMode: String,                     // 主要交通方式
    val detectedMode: String? = null,              // AI检测的交通方式

    // 环保数据
    val totalCarbon: Double,                       // 总碳排放（kg）
    val carbonSaved: Double,                       // 减少的碳排放（kg）
    val isGreenTrip: Boolean,                      // 是否为绿色出行
    val greenPoints: Int = 0,                      // 获得的绿色积分

    // 路线类型
    val routeType: String? = null,                 // 路线类型（low_carbon, balanced）

    // 备注
    val notes: String? = null                      // 用户备注
)

/**
 * 导航历史记录的简化版本（用于列表显示）
 */
data class NavigationHistorySummary(
    val id: Long,
    val startTime: Long,
    val originName: String,
    val destinationName: String,
    val totalDistance: Double,
    val transportMode: String,
    val carbonSaved: Double,
    val durationSeconds: Int
)

/**
 * 类型转换器（用于Room存储复杂类型）
 */
class NavigationHistoryConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromLatLngList(value: String): List<LatLng> {
        val type = object : TypeToken<List<Map<String, Double>>>() {}.type
        val list: List<Map<String, Double>> = gson.fromJson(value, type)
        return list.map { LatLng(it["lat"] ?: 0.0, it["lng"] ?: 0.0) }
    }

    @TypeConverter
    fun toLatLngList(list: List<LatLng>): String {
        val simplified = list.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        return gson.toJson(simplified)
    }
}
