package com.ecogo.app.service

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.ecogo.app.data.model.RouteStep
import com.ecogo.app.data.model.TransitDetails
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * Google Directions API 服务
 *
 * 用于获取两点之间的真实道路路线
 */
object DirectionsService {

    private const val TAG = "DirectionsService"
    private const val DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json"

    private var apiKey: String? = null

    /**
     * 初始化 API Key
     */
    fun init(context: Context) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY")
            Log.d(TAG, "Directions API initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get API key: ${e.message}")
        }
    }

    /**
     * 获取两点之间的路线
     *
     * @param origin 起点
     * @param destination 终点
     * @param mode 交通方式: driving, walking, bicycling, transit
     * @return 路线点列表，失败返回 null
     */
    suspend fun getRoute(
        origin: LatLng,
        destination: LatLng,
        mode: String = "walking"
    ): DirectionsResult? = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isNullOrEmpty()) {
            Log.e(TAG, "API key not initialized")
            return@withContext null
        }

        try {
            val url = buildUrl(origin, destination, mode, key)
            Log.d(TAG, "Requesting directions: $url")

            val response = URL(url).readText()
            parseDirectionsResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get directions: ${e.message}")
            null
        }
    }

    /**
     * 获取两点之间的多条路线（用于显示路线选择）
     *
     * @param origin 起点
     * @param destination 终点
     * @param mode 交通方式: driving, walking, bicycling, transit
     * @return 路线列表，失败返回空列表
     */
    suspend fun getRoutes(
        origin: LatLng,
        destination: LatLng,
        mode: String = "walking"
    ): List<DirectionsResult> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isNullOrEmpty()) {
            Log.e(TAG, "API key not initialized")
            return@withContext emptyList()
        }

        try {
            val url = buildUrl(origin, destination, mode, key, alternatives = true)
            Log.d(TAG, "Requesting alternative routes: $url")

            val response = URL(url).readText()
            parseAllDirectionsResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get alternative routes: ${e.message}")
            emptyList()
        }
    }

    /**
     * 构建 API 请求 URL
     */
    private fun buildUrl(
        origin: LatLng,
        destination: LatLng,
        mode: String,
        apiKey: String,
        alternatives: Boolean = false
    ): String {
        var url = "$DIRECTIONS_API_URL?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=$mode" +
                "&key=$apiKey"

        if (alternatives) {
            url += "&alternatives=true"
        }

        return url
    }

    /**
     * 解析 Directions API 响应
     */
    private fun parseDirectionsResponse(response: String): DirectionsResult? {
        try {
            val json = JSONObject(response)
            val status = json.getString("status")

            if (status != "OK") {
                Log.e(TAG, "Directions API error: $status")
                return null
            }

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                Log.e(TAG, "No routes found")
                return null
            }

            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val leg = legs.getJSONObject(0)

            // 获取总距离和时间
            val distance = leg.getJSONObject("distance").getInt("value") // 米
            val duration = leg.getJSONObject("duration").getInt("value") // 秒

            // 解码路线点（polyline 编码）
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPoints = overviewPolyline.getString("points")
            val routePoints = decodePolyline(encodedPoints)

            // 解析详细步骤
            val steps = parseSteps(leg.getJSONArray("steps"))

            Log.d(TAG, "Route found: ${routePoints.size} points, $distance meters, $duration seconds, ${steps.size} steps")

            return DirectionsResult(
                points = routePoints,
                distanceMeters = distance,
                durationSeconds = duration,
                steps = steps
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse directions response: ${e.message}")
            return null
        }
    }

    /**
     * 解析所有路线（用于alternatives=true的响应）
     */
    private fun parseAllDirectionsResponse(response: String): List<DirectionsResult> {
        val results = mutableListOf<DirectionsResult>()

        try {
            val json = JSONObject(response)
            val status = json.getString("status")

            if (status != "OK") {
                Log.e(TAG, "Directions API error: $status")
                return emptyList()
            }

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                Log.e(TAG, "No routes found")
                return emptyList()
            }

            // 遍历所有路线
            for (i in 0 until routes.length()) {
                try {
                    val route = routes.getJSONObject(i)
                    val legs = route.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)

                    // 获取总距离和时间
                    val distance = leg.getJSONObject("distance").getInt("value") // 米
                    val duration = leg.getJSONObject("duration").getInt("value") // 秒

                    // 解码路线点（polyline 编码）
                    val overviewPolyline = route.getJSONObject("overview_polyline")
                    val encodedPoints = overviewPolyline.getString("points")
                    val routePoints = decodePolyline(encodedPoints)

                    // 解析详细步骤
                    val steps = parseSteps(leg.getJSONArray("steps"))

                    results.add(DirectionsResult(
                        points = routePoints,
                        distanceMeters = distance,
                        durationSeconds = duration,
                        steps = steps
                    ))

                    Log.d(TAG, "Route $i: ${routePoints.size} points, $distance meters, $duration seconds, ${steps.size} steps")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse route $i: ${e.message}")
                }
            }

            Log.d(TAG, "Parsed ${results.size} routes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse all directions response: ${e.message}")
        }

        return results
    }

    /**
     * 解析详细步骤
     */
    private fun parseSteps(stepsArray: org.json.JSONArray): List<RouteStep> {
        val steps = mutableListOf<RouteStep>()

        for (i in 0 until stepsArray.length()) {
            try {
                val stepJson = stepsArray.getJSONObject(i)

                // 提取基本信息
                val instruction = stepJson.optString("html_instructions", "")
                    .replace("<[^>]*>".toRegex(), "") // 移除 HTML 标签
                val distance = stepJson.getJSONObject("distance").getDouble("value") // 米
                val duration = stepJson.getJSONObject("duration").getInt("value") // 秒
                val travelMode = stepJson.getString("travel_mode")

                // 解析公交详情（仅 TRANSIT 模式有）
                val transitDetails = if (travelMode == "TRANSIT" && stepJson.has("transit_details")) {
                    parseTransitDetails(stepJson.getJSONObject("transit_details"))
                } else {
                    null
                }

                steps.add(RouteStep(
                    instruction = instruction,
                    distance = distance,
                    duration = duration,
                    travel_mode = travelMode,
                    transit_details = transitDetails
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse step $i: ${e.message}")
            }
        }

        return steps
    }

    /**
     * 解析公交详情
     */
    private fun parseTransitDetails(transitJson: JSONObject): TransitDetails? {
        return try {
            val line = transitJson.getJSONObject("line")
            val departureStop = transitJson.getJSONObject("departure_stop")
            val arrivalStop = transitJson.getJSONObject("arrival_stop")
            val vehicle = line.getJSONObject("vehicle")

            TransitDetails(
                line_name = line.optString("name", ""),
                line_short_name = line.optString("short_name", null),
                departure_stop = departureStop.optString("name", ""),
                arrival_stop = arrivalStop.optString("name", ""),
                num_stops = transitJson.optInt("num_stops", 0),
                vehicle_type = vehicle.optString("type", "BUS"),
                headsign = transitJson.optString("headsign", null)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse transit details: ${e.message}")
            null
        }
    }

    /**
     * 解码 Google 的 Polyline 编码
     * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     */
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }
}

/**
 * Directions API 结果
 */
data class DirectionsResult(
    val points: List<LatLng>,      // 路线点列表
    val distanceMeters: Int,       // 总距离（米）
    val durationSeconds: Int,      // 预计时间（秒）
    val steps: List<RouteStep>     // 详细步骤列表
)
