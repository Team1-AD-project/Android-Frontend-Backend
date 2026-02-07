package com.ecogo.app.service

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import kotlin.math.min

/**
 * 导航管理器单例
 *
 * 功能：
 * 1. 管理导航路线
 * 2. 将用户位置匹配到路线上
 * 3. 分割已走/未走路线段
 */
object NavigationManager {

    // 完整的导航路线点
    private val _routePoints = MutableLiveData<List<LatLng>>(emptyList())
    val routePoints: LiveData<List<LatLng>> = _routePoints

    // 已走过的路线点
    private val _traveledPoints = MutableLiveData<List<LatLng>>(emptyList())
    val traveledPoints: LiveData<List<LatLng>> = _traveledPoints

    // 剩余的路线点
    private val _remainingPoints = MutableLiveData<List<LatLng>>(emptyList())
    val remainingPoints: LiveData<List<LatLng>> = _remainingPoints

    // 当前匹配到的路线点索引
    private val _currentRouteIndex = MutableLiveData<Int>(0)
    val currentRouteIndex: LiveData<Int> = _currentRouteIndex

    // 是否在导航中
    private val _isNavigating = MutableLiveData<Boolean>(false)
    val isNavigating: LiveData<Boolean> = _isNavigating

    // 剩余距离（米）
    private val _remainingDistance = MutableLiveData<Float>(0f)
    val remainingDistance: LiveData<Float> = _remainingDistance

    // 已行进距离（米）
    private val _traveledDistance = MutableLiveData<Float>(0f)
    val traveledDistance: LiveData<Float> = _traveledDistance

    // 路线匹配的距离阈值（米）- 超过这个距离认为偏离路线
    private const val ROUTE_MATCH_THRESHOLD = 50f

    // 前进检测的最小距离（米）
    private const val MIN_PROGRESS_DISTANCE = 10f

    /**
     * 设置导航路线
     */
    fun setRoute(points: List<LatLng>) {
        _routePoints.postValue(points)
        _remainingPoints.postValue(points)
        _traveledPoints.postValue(emptyList())
        _currentRouteIndex.postValue(0)
        _remainingDistance.postValue(calculateTotalDistance(points))
        _traveledDistance.postValue(0f)
    }

    /**
     * 开始导航
     */
    fun startNavigation() {
        _isNavigating.postValue(true)
        _currentRouteIndex.postValue(0)
        val points = _routePoints.value ?: emptyList()
        _remainingPoints.postValue(points)
        _traveledPoints.postValue(emptyList())
        _remainingDistance.postValue(calculateTotalDistance(points))
        _traveledDistance.postValue(0f)
    }

    /**
     * 停止导航
     */
    fun stopNavigation() {
        _isNavigating.postValue(false)
    }

    /**
     * 清除导航
     */
    fun clearNavigation() {
        _isNavigating.postValue(false)
        _routePoints.postValue(emptyList())
        _traveledPoints.postValue(emptyList())
        _remainingPoints.postValue(emptyList())
        _currentRouteIndex.postValue(0)
        _remainingDistance.postValue(0f)
        _traveledDistance.postValue(0f)
    }

    /**
     * 更新用户位置并匹配路线
     *
     * @param userLocation 用户当前位置
     * @return 是否在路线上（未偏离）
     */
    fun updateLocation(userLocation: LatLng): Boolean {
        if (_isNavigating.value != true) return false

        val route = _routePoints.value ?: return false
        if (route.isEmpty()) return false

        val currentIndex = _currentRouteIndex.value ?: 0

        // 找到用户位置最近的路线点（只在当前点之后搜索，防止回退）
        val matchResult = findNearestPointOnRoute(userLocation, route, currentIndex)
        val nearestIndex = matchResult.first
        val distanceToRoute = matchResult.second

        // 检查是否偏离路线
        val isOnRoute = distanceToRoute < ROUTE_MATCH_THRESHOLD

        // 只有前进时才更新（防止GPS抖动导致回退）
        if (nearestIndex >= currentIndex) {
            _currentRouteIndex.postValue(nearestIndex)

            // 分割已走和未走的路线
            val traveled = route.subList(0, min(nearestIndex + 1, route.size))
            val remaining = if (nearestIndex < route.size - 1) {
                // 在剩余路线开头加入当前位置，使路线连续
                listOf(userLocation) + route.subList(nearestIndex + 1, route.size)
            } else {
                listOf(userLocation)
            }

            _traveledPoints.postValue(traveled)
            _remainingPoints.postValue(remaining)

            // 更新距离
            _traveledDistance.postValue(calculateTotalDistance(traveled))
            _remainingDistance.postValue(calculateTotalDistance(remaining))
        }

        return isOnRoute
    }

    /**
     * 在路线上找到离用户最近的点
     *
     * @param userLocation 用户位置
     * @param route 路线点列表
     * @param startIndex 开始搜索的索引
     * @return Pair<最近点索引, 距离>
     */
    private fun findNearestPointOnRoute(
        userLocation: LatLng,
        route: List<LatLng>,
        startIndex: Int
    ): Pair<Int, Float> {
        var nearestIndex = startIndex
        var minDistance = Float.MAX_VALUE

        // 只向前搜索一定范围内的点（避免跳跃太远）
        val searchRange = min(startIndex + 20, route.size)

        for (i in startIndex until searchRange) {
            val distance = calculateDistanceBetween(userLocation, route[i])
            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = i
            }
        }

        // 如果在当前点附近没找到更近的，检查是否已经接近下一个点
        if (nearestIndex == startIndex && startIndex < route.size - 1) {
            val distanceToNext = calculateDistanceBetween(userLocation, route[startIndex + 1])
            val distanceToCurrent = calculateDistanceBetween(userLocation, route[startIndex])

            // 如果离下一个点更近，并且已经走过了当前点
            if (distanceToNext < distanceToCurrent && distanceToCurrent > MIN_PROGRESS_DISTANCE) {
                nearestIndex = startIndex + 1
                minDistance = distanceToNext
            }
        }

        return Pair(nearestIndex, minDistance)
    }

    /**
     * 计算两点之间的距离（米）
     */
    private fun calculateDistanceBetween(p1: LatLng, p2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0]
    }

    /**
     * 计算路线总距离（米）
     */
    private fun calculateTotalDistance(points: List<LatLng>): Float {
        if (points.size < 2) return 0f

        var total = 0f
        for (i in 1 until points.size) {
            total += calculateDistanceBetween(points[i - 1], points[i])
        }
        return total
    }

    /**
     * 获取导航进度百分比
     */
    fun getProgressPercentage(): Float {
        val total = _routePoints.value?.size ?: 0
        val current = _currentRouteIndex.value ?: 0
        return if (total > 0) (current.toFloat() / total * 100) else 0f
    }

    /**
     * 检查是否已到达目的地
     */
    fun hasReachedDestination(): Boolean {
        val route = _routePoints.value ?: return false
        val currentIndex = _currentRouteIndex.value ?: 0
        return currentIndex >= route.size - 1
    }
}
