package com.ecogo.app.ui.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecogo.R
import com.ecogo.app.data.local.entity.NavigationHistory
import com.ecogo.app.data.repository.NavigationHistoryRepository
import com.ecogo.databinding.ActivityHistoryMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 历史路线地图展示页面
 * 用于在地图上显示用户过往的导航路径
 *
 * 使用方法：
 * ```kotlin
 * val intent = Intent(this, HistoryMapActivity::class.java)
 * intent.putExtra("HISTORY_ID", historyId)
 * startActivity(intent)
 * ```
 */
class HistoryMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityHistoryMapBinding
    private var googleMap: GoogleMap? = null
    private val repository = NavigationHistoryRepository.getInstance()

    // 当前显示的历史记录
    private var currentHistory: NavigationHistory? = null

    // 地图上的标记和路线
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var plannedRoutePolyline: Polyline? = null
    private var actualTrackPolyline: Polyline? = null

    companion object {
        const val EXTRA_HISTORY_ID = "HISTORY_ID"
        const val TAG = "HistoryMapActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化地图
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupUI()
    }

    private fun setupUI() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 显示/隐藏规划路线
        binding.btnTogglePlannedRoute.setOnClickListener {
            togglePlannedRoute()
        }

        // 显示/隐藏实际轨迹
        binding.btnToggleActualTrack.setOnClickListener {
            toggleActualTrack()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 配置地图
        map.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = false
        }

        // 加载历史记录
        val historyId = intent.getLongExtra(EXTRA_HISTORY_ID, -1)
        if (historyId != -1L) {
            loadHistory(historyId)
        } else {
            Toast.makeText(this, "无效的记录ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * 加载历史记录并显示在地图上
     */
    private fun loadHistory(historyId: Long) {
        lifecycleScope.launch {
            try {
                val history = repository.getHistoryById(historyId)
                if (history != null) {
                    currentHistory = history
                    displayHistoryOnMap(history)
                    updateInfoCard(history)
                } else {
                    Toast.makeText(this@HistoryMapActivity, "未找到记录", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistoryMapActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * 在地图上显示历史记录
     */
    private fun displayHistoryOnMap(history: NavigationHistory) {
        // 清除之前的标记和路线
        clearMap()

        // 1. 显示起点和终点标记
        val origin = LatLng(history.originLat, history.originLng)
        val destination = LatLng(history.destinationLat, history.destinationLng)

        originMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(origin)
                .title("起点: ${history.originName}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        destinationMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(destination)
                .title("终点: ${history.destinationName}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // 2. 解析并显示规划路线（蓝色虚线）
        try {
            val routePoints = repository.parseLatLngListFromJson(history.routePoints)
            if (routePoints.isNotEmpty()) {
                drawPlannedRoute(routePoints)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to parse route points", e)
        }

        // 3. 解析并显示实际轨迹（绿色实线）
        try {
            val trackPoints = repository.parseLatLngListFromJson(history.trackPoints)
            if (trackPoints.isNotEmpty()) {
                drawActualTrack(trackPoints)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to parse track points", e)
        }

        // 4. 调整相机显示完整路线
        fitMapToRoute(origin, destination)
    }

    /**
     * 绘制规划路线（蓝色虚线）
     */
    private fun drawPlannedRoute(points: List<LatLng>) {
        plannedRoutePolyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(8f)
                .color(getColor(R.color.route_planned)) // 蓝色
                .geodesic(true)
                .pattern(listOf(Dash(20f), Gap(10f))) // 虚线
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * 绘制实际轨迹（绿色实线）
     */
    private fun drawActualTrack(points: List<LatLng>) {
        actualTrackPolyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(10f)
                .color(getColor(R.color.green_primary)) // 绿色
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * 调整相机显示完整路线
     */
    private fun fitMapToRoute(origin: LatLng, destination: LatLng) {
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(origin)
        boundsBuilder.include(destination)

        // 如果有轨迹点，也包含进来
        currentHistory?.let { history ->
            try {
                val trackPoints = repository.parseLatLngListFromJson(history.trackPoints)
                trackPoints.forEach { boundsBuilder.include(it) }
            } catch (e: Exception) {
                // 忽略错误
            }
        }

        val bounds = boundsBuilder.build()
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    /**
     * 更新信息卡片
     */
    private fun updateInfoCard(history: NavigationHistory) {
        binding.apply {
            tvRouteName.text = "${history.originName} → ${history.destinationName}"

            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(Date(history.startTime))
            tvDate.text = date

            tvDistance.text = String.format("%.2f km", history.totalDistance / 1000)
            tvDuration.text = formatDuration(history.durationSeconds)
            tvTransportMode.text = getTransportModeDisplay(history.transportMode)

            if (history.carbonSaved > 0) {
                tvCarbonSaved.text = String.format("减碳 %.2f kg", history.carbonSaved)
                tvCarbonSaved.setTextColor(getColor(R.color.green_primary))
            } else {
                tvCarbonSaved.text = String.format("碳排放 %.2f kg", history.totalCarbon)
                tvCarbonSaved.setTextColor(getColor(R.color.text_secondary))
            }

            // 显示AI检测的交通方式（如果有）
            if (history.detectedMode != null && history.detectedMode != history.transportMode) {
                tvDetectedMode.visibility = android.view.View.VISIBLE
                tvDetectedMode.text = "AI检测: ${getTransportModeDisplay(history.detectedMode!!)}"
            } else {
                tvDetectedMode.visibility = android.view.View.GONE
            }
        }
    }

    /**
     * 切换规划路线显示
     */
    private fun togglePlannedRoute() {
        plannedRoutePolyline?.let {
            it.isVisible = !it.isVisible
            val status = if (it.isVisible) "显示" else "隐藏"
            Toast.makeText(this, "规划路线已${status}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 切换实际轨迹显示
     */
    private fun toggleActualTrack() {
        actualTrackPolyline?.let {
            it.isVisible = !it.isVisible
            val status = if (it.isVisible) "显示" else "隐藏"
            Toast.makeText(this, "实际轨迹已${status}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 清除地图上的所有标记和路线
     */
    private fun clearMap() {
        originMarker?.remove()
        destinationMarker?.remove()
        plannedRoutePolyline?.remove()
        actualTrackPolyline?.remove()

        originMarker = null
        destinationMarker = null
        plannedRoutePolyline = null
        actualTrackPolyline = null
    }

    /**
     * 格式化时长
     */
    private fun formatDuration(durationSeconds: Int): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60

        return when {
            hours > 0 -> String.format("%d小时%d分钟", hours, minutes)
            minutes > 0 -> String.format("%d分钟", minutes)
            else -> "不到1分钟"
        }
    }

    /**
     * 获取交通方式显示文本
     */
    private fun getTransportModeDisplay(mode: String): String {
        return when (mode.lowercase()) {
            "walking" -> "🚶 步行"
            "cycling" -> "🚴 骑行"
            "bus" -> "🚌 公交"
            "subway" -> "🚇 地铁"
            "driving" -> "🚗 驾车"
            else -> mode
        }
    }
}
