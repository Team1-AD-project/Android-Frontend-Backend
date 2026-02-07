package com.ecogo.app.data.model

/**
 * ========================================
 * 路线推荐相关模型
 * 对应 RecommendService 微服务接口
 * ========================================
 */

/**
 * 路线推荐请求
 * POST /api/mobile/route/recommend/low-carbon
 * POST /api/mobile/route/recommend/balance
 */
data class RouteRecommendRequest(
    val user_id: String,
    val start_point: GeoPoint,
    val end_point: GeoPoint
)

/**
 * 路线推荐响应
 */
data class RouteRecommendData(
    val route_id: String? = null,
    val route_type: String? = null,
    val total_distance: Double = 0.0,
    val estimated_duration: Int = 0,  // 分钟
    val total_carbon: Double = 0.0,
    val carbon_saved: Double = 0.0,
    val route_segments: List<RouteSegment>? = null,
    val route_points: List<GeoPoint>? = null,
    val route_steps: List<RouteStep>? = null,  // 详细步骤（用于公交等多模式路线）
    val route_alternatives: List<RouteAlternative>? = null,  // 多条路线选项（仅公交模式）
    // 兼容旧字段
    val green_route: List<GeoPoint>? = null,
    val duration: Int? = null
)

/**
 * 路线选项（用于显示多条路线供用户选择）
 */
data class RouteAlternative(
    val index: Int,                      // 路线索引
    val total_distance: Double,          // 总距离（公里）
    val estimated_duration: Int,         // 预计时长（分钟）
    val total_carbon: Double,            // 碳排放
    val route_points: List<GeoPoint>,    // 路线点
    val route_steps: List<RouteStep>,    // 详细步骤
    val summary: String                  // 路线摘要（如"地铁1号线 → 公交46路"）
)

/**
 * 路线段（多段式路线中的单段）
 */
data class RouteSegment(
    val transport_mode: String,
    val distance: Double,
    val duration: Int,
    val carbon_emission: Double,
    val instructions: String? = null,
    val polyline: List<GeoPoint>? = null
)

/**
 * 路线推荐类型
 */
enum class RouteType(val value: String) {
    LOW_CARBON("low-carbon"),      // 碳排最低
    BALANCE("balance")              // 时间-碳排平衡
}

/**
 * 路线缓存数据
 * GET /api/mobile/route/cache/{user_id}
 */
data class RouteCacheData(
    val route_info: RouteRecommendData?,
    val expire_time: String?
)

/**
 * 交通方式
 */
enum class TransportMode(val value: String, val displayName: String) {
    WALKING("walking", "步行"),
    CYCLING("cycling", "骑行"),
    BUS("bus", "公交"),
    SUBWAY("subway", "地铁"),
    DRIVING("driving", "驾车")
}

/**
 * 路线详细步骤（用于显示公交换乘等详细信息）
 */
data class RouteStep(
    val instruction: String,           // 步骤说明（如"步行至公交站"、"乘坐X路公交"）
    val distance: Double,               // 距离（米）
    val duration: Int,                  // 时长（秒）
    val travel_mode: String,            // 出行方式（WALKING, TRANSIT, DRIVING等）
    val transit_details: TransitDetails? = null  // 公交详情（仅 TRANSIT 模式有）
)

/**
 * 公交详情
 */
data class TransitDetails(
    val line_name: String,              // 线路名称（如"地铁1号线"、"公交46路"）
    val line_short_name: String? = null, // 线路简称（如"1号线"、"46路"）
    val departure_stop: String,         // 上车站点
    val arrival_stop: String,           // 下车站点
    val num_stops: Int,                 // 经过站数
    val vehicle_type: String,           // 车辆类型（BUS, SUBWAY, RAIL等）
    val headsign: String? = null        // 方向标识（如"往XX方向"）
)
