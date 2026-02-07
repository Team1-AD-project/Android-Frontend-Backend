package com.ecogo.app.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecogo.app.data.model.*
import com.ecogo.app.data.repository.IMapRepository
import com.ecogo.app.data.repository.MapRepository
import com.ecogo.app.data.repository.MockMapRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 地图 ViewModel
 * 管理地图页面的 UI 状态和业务逻辑
 *
 * @param useMockData 是否使用 Mock 数据（默认 true 用于测试）
 */
class MapViewModel(
    useMockData: Boolean = true  // 开关：true = Mock数据，false = 真实API
) : ViewModel() {

    // 根据开关选择 Repository
    private val repository: IMapRepository = if (useMockData) {
        MockMapRepository()
    } else {
        MapRepository()
    }

    // 模拟用户 ID (实际应从登录状态获取)
    private val userId: String = "test-user-001"

    // ========================================
    // UI 状态
    // ========================================

    // 当前位置
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    // 起点（用于路线规划）
    private val _origin = MutableLiveData<LatLng?>()
    val origin: LiveData<LatLng?> = _origin

    // 目的地
    private val _destination = MutableLiveData<LatLng?>()
    val destination: LiveData<LatLng?> = _destination

    // 行程状态
    private val _tripState = MutableLiveData<TripState>(TripState.Idle)
    val tripState: LiveData<TripState> = _tripState

    // 当前行程 ID
    private val _currentTripId = MutableLiveData<String?>()
    val currentTripId: LiveData<String?> = _currentTripId

    // 推荐路线
    private val _recommendedRoute = MutableLiveData<RouteRecommendData?>()
    val recommendedRoute: LiveData<RouteRecommendData?> = _recommendedRoute

    // 路线点 (用于绘制 Polyline)
    private val _routePoints = MutableLiveData<List<LatLng>>()
    val routePoints: LiveData<List<LatLng>> = _routePoints

    // 碳足迹计算结果
    private val _carbonResult = MutableLiveData<CarbonCalculateData?>()
    val carbonResult: LiveData<CarbonCalculateData?> = _carbonResult

    // 错误消息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 成功消息（用于 Toast）
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // 当前选择的交通方式
    private val _selectedTransportMode = MutableLiveData<TransportMode?>(null)
    val selectedTransportMode: LiveData<TransportMode?> = _selectedTransportMode

    // ========================================
    // 位置更新
    // ========================================

    /**
     * 更新当前位置
     */
    fun updateCurrentLocation(latLng: LatLng) {
        _currentLocation.value = latLng
        // 如果还没有设置起点，默认使用当前位置
        if (_origin.value == null) {
            _origin.value = latLng
        }
    }

    /**
     * 设置起点
     */
    fun setOrigin(latLng: LatLng) {
        _origin.value = latLng
    }

    /**
     * 设置目的地
     */
    fun setDestination(latLng: LatLng) {
        _destination.value = latLng
    }

    /**
     * 交换起点和终点
     */
    fun swapOriginDestination() {
        val tempOrigin = _origin.value
        val tempDest = _destination.value

        if (tempDest != null) {
            _origin.value = tempDest
        }
        if (tempOrigin != null) {
            _destination.value = tempOrigin
        }

        // 如果有路线，需要重新获取
        if (_recommendedRoute.value != null) {
            _recommendedRoute.value = null
            _routePoints.value = emptyList()
        }
    }

    /**
     * 清除目的地
     */
    fun clearDestination() {
        _destination.value = null
        _recommendedRoute.value = null
        _routePoints.value = emptyList()
    }

    // ========================================
    // 行程追踪
    // ========================================

    /**
     * 开始行程追踪
     */
    fun startTracking() {
        val location = _currentLocation.value ?: run {
            _errorMessage.value = "无法获取当前位置"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _tripState.value = TripState.Starting

            val startPoint = GeoPoint.fromLatLng(location)
            val result = repository.startTripTracking(userId, startPoint)

            result.fold(
                onSuccess = { data ->
                    _currentTripId.value = data.trip_id
                    _tripState.value = TripState.Tracking(data.trip_id)
                    _successMessage.value = data.message ?: "行程已开始"
                },
                onFailure = { error ->
                    _tripState.value = TripState.Idle
                    _errorMessage.value = error.message
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * 停止行程追踪并保存
     */
    fun stopTracking() {
        val tripId = _currentTripId.value ?: run {
            _errorMessage.value = "没有正在进行的行程"
            return
        }
        val endLocation = _currentLocation.value ?: run {
            _errorMessage.value = "无法获取当前位置"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _tripState.value = TripState.Stopping

            val endPoint = GeoPoint.fromLatLng(endLocation)
            val endTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Date())

            // TODO: 计算实际距离
            val distance = 1.5 // 示例距离 (km)

            val result = repository.saveTrip(
                tripId = tripId,
                userId = userId,
                endPoint = endPoint,
                distance = distance,
                endTime = endTime
            )

            result.fold(
                onSuccess = { data ->
                    // 保存成功后计算碳足迹
                    calculateCarbon(tripId, listOf(TransportMode.WALKING.value))
                    _tripState.value = TripState.Completed
                    _currentTripId.value = null
                    _successMessage.value = data.message ?: "行程已保存"
                },
                onFailure = { error ->
                    _tripState.value = TripState.Tracking(tripId)
                    _errorMessage.value = error.message
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * 取消行程
     */
    fun cancelTracking(reason: String? = null) {
        val tripId = _currentTripId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.cancelTripTracking(tripId, userId, reason)

            result.fold(
                onSuccess = { data ->
                    _tripState.value = TripState.Idle
                    _currentTripId.value = null
                    _successMessage.value = data.message ?: "行程已取消"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
            _isLoading.value = false
        }
    }

    // ========================================
    // 路线推荐
    // ========================================

    /**
     * 获取低碳路线
     */
    fun fetchLowCarbonRoute() {
        val start = _origin.value ?: _currentLocation.value ?: run {
            _errorMessage.value = "无法获取起点位置"
            return
        }
        val end = _destination.value ?: run {
            _errorMessage.value = "请先设置目的地"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.getLowestCarbonRoute(
                userId = userId,
                startPoint = GeoPoint.fromLatLng(start),
                endPoint = GeoPoint.fromLatLng(end)
            )

            result.fold(
                onSuccess = { data ->
                    _recommendedRoute.value = data
                    // 优先使用新的 route_points，兼容旧的 green_route
                    val points = data.route_points ?: data.green_route ?: emptyList()
                    _routePoints.value = points.map { it.toLatLng() }
                    _successMessage.value = "已找到低碳路线，预计节省 ${String.format("%.2f", data.carbon_saved)} kg 碳排放"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * 获取平衡路线
     */
    fun fetchBalancedRoute() {
        val start = _origin.value ?: _currentLocation.value ?: run {
            _errorMessage.value = "无法获取起点位置"
            return
        }
        val end = _destination.value ?: run {
            _errorMessage.value = "请先设置目的地"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.getBalancedRoute(
                userId = userId,
                startPoint = GeoPoint.fromLatLng(start),
                endPoint = GeoPoint.fromLatLng(end)
            )

            result.fold(
                onSuccess = { data ->
                    _recommendedRoute.value = data
                    val points = data.route_points ?: data.green_route ?: emptyList()
                    _routePoints.value = points.map { it.toLatLng() }
                    _successMessage.value = "已找到平衡路线，预计节省 ${String.format("%.2f", data.carbon_saved)} kg 碳排放"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * 根据交通方式获取路线
     */
    fun fetchRouteByMode(mode: TransportMode) {
        // 记录选择的交通方式
        _selectedTransportMode.value = mode

        val start = _origin.value ?: _currentLocation.value ?: run {
            _errorMessage.value = "无法获取起点位置"
            return
        }
        val end = _destination.value ?: run {
            _errorMessage.value = "请先设置目的地"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.getRouteByTransportMode(
                userId = userId,
                startPoint = GeoPoint.fromLatLng(start),
                endPoint = GeoPoint.fromLatLng(end),
                transportMode = mode
            )

            result.fold(
                onSuccess = { data ->
                    _recommendedRoute.value = data
                    val points = data.route_points ?: emptyList()
                    _routePoints.value = points.map { it.toLatLng() }

                    val distance = String.format("%.2f", data.total_distance)
                    val duration = data.estimated_duration
                    val carbonSaved = String.format("%.2f", data.carbon_saved)
                    _successMessage.value = "${mode.displayName}路线: ${distance}公里, 预计${duration}分钟, 减碳${carbonSaved}kg"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "路线获取失败"
                }
            )
            _isLoading.value = false
        }
    }

    // ========================================
    // 碳足迹计算
    // ========================================

    /**
     * 计算碳足迹
     */
    private fun calculateCarbon(tripId: String, transportModes: List<String>) {
        viewModelScope.launch {
            val result = repository.calculateCarbon(tripId, transportModes)

            result.fold(
                onSuccess = { data ->
                    _carbonResult.value = data
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

/**
 * 行程状态
 */
sealed class TripState {
    object Idle : TripState()                       // 空闲
    object Starting : TripState()                   // 正在开始
    data class Tracking(val tripId: String) : TripState()  // 追踪中
    object Stopping : TripState()                   // 正在停止
    object Completed : TripState()                  // 已完成
}
