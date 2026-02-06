package com.ecogo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecogo.data.*
import com.ecogo.utils.CarbonCalculator
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 导航ViewModel
 * 管理导航相关的状态和数据
 */
class NavigationViewModel : ViewModel() {
    
    // 导航状态
    private val _navigationState = MutableLiveData(NavigationState.IDLE)
    val navigationState: LiveData<NavigationState> = _navigationState
    
    // 当前路线
    private val _currentRoute = MutableLiveData<NavRoute?>()
    val currentRoute: LiveData<NavRoute?> = _currentRoute
    
    // 路线选项列表
    private val _routeOptions = MutableLiveData<List<NavRoute>>()
    val routeOptions: LiveData<List<NavRoute>> = _routeOptions
    
    // 当前行程
    private val _currentTrip = MutableLiveData<Trip?>()
    val currentTrip: LiveData<Trip?> = _currentTrip
    
    // 行程状态
    private val _tripStatus = MutableLiveData<TripStatus>()
    val tripStatus: LiveData<TripStatus> = _tripStatus
    
    // 历史行程列表
    private val _tripHistory = MutableLiveData<List<Trip>>(emptyList())
    val tripHistory: LiveData<List<Trip>> = _tripHistory
    
    // 选中的起点和终点
    private val _selectedOrigin = MutableLiveData<NavLocation?>()
    val selectedOrigin: LiveData<NavLocation?> = _selectedOrigin
    
    private val _selectedDestination = MutableLiveData<NavLocation?>()
    val selectedDestination: LiveData<NavLocation?> = _selectedDestination
    
    // 选中的交通方式
    private val _selectedMode = MutableLiveData(TransportMode.WALK)
    val selectedMode: LiveData<TransportMode> = _selectedMode
    
    // 实时碳排放节省
    private val _realTimeCarbonSaved = MutableLiveData(0.0)
    val realTimeCarbonSaved: LiveData<Double> = _realTimeCarbonSaved
    
    // 实时积分
    private val _realTimePoints = MutableLiveData(0)
    val realTimePoints: LiveData<Int> = _realTimePoints
    
    /**
     * 设置起点
     */
    fun setOrigin(location: NavLocation) {
        _selectedOrigin.value = location
        // 如果起点和终点都已选择，自动计算路线
        if (_selectedDestination.value != null) {
            calculateRoutes()
        }
    }
    
    /**
     * 设置终点
     */
    fun setDestination(location: NavLocation) {
        _selectedDestination.value = location
        // 如果起点和终点都已选择，自动计算路线
        if (_selectedOrigin.value != null) {
            calculateRoutes()
        }
    }
    
    /**
     * 设置交通方式
     */
    fun setTransportMode(mode: TransportMode) {
        _selectedMode.value = mode
        // 重新计算路线
        if (_selectedOrigin.value != null && _selectedDestination.value != null) {
            calculateRoutes()
        }
    }
    
    /**
     * 计算路线（多种方案）
     */
    fun calculateRoutes() {
        val origin = _selectedOrigin.value ?: return
        val destination = _selectedDestination.value ?: return
        val mode = _selectedMode.value ?: TransportMode.WALK
        
        _navigationState.value = NavigationState.PLANNING
        
        viewModelScope.launch {
            try {
                // TODO: 调用真实的路线计算API
                // 现在使用模拟数据
                val routes = generateMockRoutes(origin, destination, mode)
                _routeOptions.value = routes
                
                // 设置推荐路线为当前路线
                routes.firstOrNull { it.isRecommended }?.let {
                    _currentRoute.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _navigationState.value = NavigationState.IDLE
            }
        }
    }
    
    /**
     * 选择路线
     */
    fun selectRoute(route: NavRoute) {
        _currentRoute.value = route
    }
    
    /**
     * 开始导航
     */
    fun startNavigation() {
        val route = _currentRoute.value ?: return
        
        val trip = Trip(
            id = UUID.randomUUID().toString(),
            route = route,
            startTime = System.currentTimeMillis(),
            endTime = null,
            status = TripStatus.ACTIVE,
            actualDistance = 0.0,
            actualCarbonSaved = 0.0,
            pointsEarned = 0
        )
        
        _currentTrip.value = trip
        _tripStatus.value = TripStatus.ACTIVE
        _navigationState.value = NavigationState.NAVIGATING
        _realTimeCarbonSaved.value = 0.0
        _realTimePoints.value = 0
    }
    
    /**
     * 暂停导航
     */
    fun pauseNavigation() {
        _currentTrip.value?.let { trip ->
            _currentTrip.value = trip.copy(status = TripStatus.PAUSED)
            _tripStatus.value = TripStatus.PAUSED
        }
    }
    
    /**
     * 恢复导航
     */
    fun resumeNavigation() {
        _currentTrip.value?.let { trip ->
            _currentTrip.value = trip.copy(status = TripStatus.ACTIVE)
            _tripStatus.value = TripStatus.ACTIVE
        }
    }
    
    /**
     * 结束导航
     */
    fun endNavigation() {
        _currentTrip.value?.let { trip ->
            val completedTrip = trip.copy(
                endTime = System.currentTimeMillis(),
                status = TripStatus.COMPLETED,
                actualCarbonSaved = _realTimeCarbonSaved.value ?: trip.route.carbonSaved,
                pointsEarned = _realTimePoints.value ?: trip.route.points
            )
            
            _currentTrip.value = completedTrip
            _tripStatus.value = TripStatus.COMPLETED
            _navigationState.value = NavigationState.COMPLETED
            
            // 保存到历史记录
            saveTripToHistory(completedTrip)
        }
    }
    
    /**
     * 取消导航
     */
    fun cancelNavigation() {
        _currentTrip.value?.let { trip ->
            _currentTrip.value = trip.copy(
                status = TripStatus.CANCELLED,
                endTime = System.currentTimeMillis()
            )
            _tripStatus.value = TripStatus.CANCELLED
        }
        
        resetNavigation()
    }
    
    /**
     * 重置导航状态
     */
    fun resetNavigation() {
        _navigationState.value = NavigationState.IDLE
        _currentRoute.value = null
        _currentTrip.value = null
        _selectedOrigin.value = null
        _selectedDestination.value = null
        _realTimeCarbonSaved.value = 0.0
        _realTimePoints.value = 0
    }
    
    /**
     * 更新实时碳排放节省
     */
    fun updateRealTimeCarbonSaved(distance: Double) {
        val mode = _currentRoute.value?.mode ?: TransportMode.WALK
        val carbonSaved = CarbonCalculator.calculateSavings(mode, distance / 1000.0)
        _realTimeCarbonSaved.value = carbonSaved
        _realTimePoints.value = CarbonCalculator.calculatePoints(carbonSaved)
    }
    
    /**
     * 保存行程到历史记录
     */
    private fun saveTripToHistory(trip: Trip) {
        val currentHistory = _tripHistory.value?.toMutableList() ?: mutableListOf()
        currentHistory.add(0, trip) // 添加到列表开头
        _tripHistory.value = currentHistory
        
        // TODO: 持久化到数据库或SharedPreferences
    }
    
    /**
     * 获取行程历史记录
     */
    fun loadTripHistory() {
        viewModelScope.launch {
            // TODO: 从数据库或SharedPreferences加载
            // 现在使用空列表
        }
    }
    
    /**
     * 生成模拟路线数据
     */
    private fun generateMockRoutes(
        origin: NavLocation,
        destination: NavLocation,
        preferredMode: TransportMode
    ): List<NavRoute> {
        val routes = mutableListOf<NavRoute>()
        
        // 计算直线距离（粗略估算）
        val distance = calculateMockDistance(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude
        )
        
        // 生成步行路线
        routes.add(generateRoute(origin, destination, TransportMode.WALK, distance, "🌿 最环保"))
        
        // 生成骑行路线
        routes.add(generateRoute(origin, destination, TransportMode.CYCLE, distance, "⚡ 最快"))
        
        // 生成公交路线
        routes.add(generateRoute(origin, destination, TransportMode.BUS, distance * 1.2, "⚖️ 平衡"))
        
        return routes
    }
    
    private fun generateRoute(
        origin: NavLocation,
        destination: NavLocation,
        mode: TransportMode,
        distance: Double,
        badge: String
    ): NavRoute {
        val distanceKm = distance / 1000.0
        val duration = calculateDuration(mode, distanceKm)
        val carbonEmission = CarbonCalculator.calculateEmission(mode, distanceKm)
        val carbonSaved = CarbonCalculator.calculateSavings(mode, distanceKm)
        val points = CarbonCalculator.calculatePoints(carbonSaved)
        
        return NavRoute(
            id = UUID.randomUUID().toString(),
            origin = origin,
            destination = destination,
            mode = mode,
            distance = distanceKm,
            duration = duration,
            carbonEmission = carbonEmission,
            carbonSaved = carbonSaved,
            points = points,
            steps = emptyList(),
            polyline = "",
            isRecommended = mode == TransportMode.WALK || badge.contains("环保"),
            badge = badge
        )
    }
    
    private fun calculateMockDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // 简化的距离计算（米）
        val latDiff = Math.abs(lat2 - lat1) * 111000
        val lonDiff = Math.abs(lon2 - lon1) * 111000 * Math.cos(Math.toRadians(lat1))
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff)
    }
    
    private fun calculateDuration(mode: TransportMode, distanceKm: Double): Int {
        // 根据交通方式估算时间（分钟）
        val speed = when (mode) {
            TransportMode.WALK -> 5.0    // 5 km/h
            TransportMode.CYCLE -> 15.0  // 15 km/h
            TransportMode.BUS -> 20.0    // 20 km/h
            TransportMode.MIXED -> 12.0  // 12 km/h
        }
        return ((distanceKm / speed) * 60).toInt()
    }
}
