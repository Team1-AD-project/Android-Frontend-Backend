package com.ecogo.app.service

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

/**
 * 位置管理器单例
 * 用于在 Service 和 Activity 之间共享位置数据
 */
object LocationManager {

    // 当前位置
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    // 轨迹点列表
    private val _trackPoints = MutableLiveData<List<LatLng>>(emptyList())
    val trackPoints: LiveData<List<LatLng>> = _trackPoints

    // 总距离（米）
    private val _totalDistance = MutableLiveData<Float>(0f)
    val totalDistance: LiveData<Float> = _totalDistance

    // 追踪状态
    private val _isTracking = MutableLiveData<Boolean>(false)
    val isTracking: LiveData<Boolean> = _isTracking

    // 上一个位置点（用于计算距离）
    private var lastLocation: Location? = null

    /**
     * 开始追踪
     */
    fun startTracking() {
        _isTracking.postValue(true)
        _trackPoints.postValue(emptyList())
        _totalDistance.postValue(0f)
        lastLocation = null
    }

    /**
     * 停止追踪
     */
    fun stopTracking() {
        _isTracking.postValue(false)
        lastLocation = null
    }

    /**
     * 更新位置
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        val newLatLng = LatLng(latitude, longitude)
        _currentLocation.postValue(newLatLng)

        // 如果正在追踪，添加到轨迹
        if (_isTracking.value == true) {
            addTrackPoint(latitude, longitude)
        }
    }

    /**
     * 添加轨迹点
     */
    private fun addTrackPoint(latitude: Double, longitude: Double) {
        val newLatLng = LatLng(latitude, longitude)
        val currentList = _trackPoints.value?.toMutableList() ?: mutableListOf()
        currentList.add(newLatLng)
        _trackPoints.postValue(currentList)

        // 计算距离
        val newLocation = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        lastLocation?.let { last ->
            val distance = last.distanceTo(newLocation)
            // 只有移动超过 5 米才计入距离（过滤 GPS 漂移）
            if (distance > 5) {
                val currentDistance = _totalDistance.value ?: 0f
                _totalDistance.postValue(currentDistance + distance)
                lastLocation = newLocation
            }
        } ?: run {
            lastLocation = newLocation
        }
    }

    /**
     * 清除轨迹
     */
    fun clearTrack() {
        _trackPoints.postValue(emptyList())
        _totalDistance.postValue(0f)
        lastLocation = null
    }

    /**
     * 获取当前轨迹点数量
     */
    fun getTrackPointCount(): Int = _trackPoints.value?.size ?: 0

    /**
     * 获取总距离（公里）
     */
    fun getTotalDistanceKm(): Float = (_totalDistance.value ?: 0f) / 1000f
}
