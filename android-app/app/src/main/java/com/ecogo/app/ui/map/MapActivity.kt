package com.ecogo.app.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.ecogo.R
import com.ecogo.databinding.ActivityMapBinding
import com.ecogo.app.data.model.TransportMode
import com.ecogo.app.service.DirectionsService
import com.ecogo.app.service.LocationManager
import com.ecogo.app.service.LocationTrackingService
import com.ecogo.app.service.NavigationManager
import com.ecogo.app.ml.TransportModeDetector
import com.ecogo.app.data.repository.NavigationHistoryRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

/**
 * 地图主页面
 * 实现 Google Maps 集成、行程追踪、路线推荐、地点搜索
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var transportModeDetector: TransportModeDetector

    // 地图标记
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var routePolyline: Polyline? = null

    // 实时轨迹
    private var trackPolyline: Polyline? = null
    private var isFollowingUser = true  // 是否跟随用户位置

    // 导航路线（已走/未走）
    private var traveledPolyline: Polyline? = null    // 已走过的路线（灰色）
    private var remainingPolyline: Polyline? = null   // 剩余路线（蓝色）
    private var isNavigationMode = false              // 是否在导航模式

    // 路线步骤适配器
    private val routeStepAdapter = RouteStepAdapter()

    // 路线选择适配器
    private val routeOptionAdapter = RouteOptionAdapter { selectedRoute ->
        onRouteSelected(selectedRoute)
    }

    // 起点和终点位置
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var originName: String = "我的位置"
    private var destinationName: String = ""

    // 标记当前搜索的是起点还是终点
    private var isSearchingOrigin = false

    // 里程碑追踪（用于显示鼓励信息）
    private val milestones = listOf(1000f, 2000f, 3000f, 5000f, 10000f) // 单位：米
    private var reachedMilestones = mutableSetOf<Float>()

    // 导航记录相关
    private var navigationStartTime: Long = 0  // 导航开始时间
    private var detectedTransportMode: String? = null  // AI检测到的交通方式

    // 行程计时器
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerStartTime = 0L
    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = SystemClock.elapsedRealtime() - timerStartTime
            val seconds = (elapsed / 1000) % 60
            val minutes = (elapsed / 1000 / 60) % 60
            val hours = elapsed / 1000 / 3600
            val timeStr = if (hours > 0)
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            else
                String.format("%02d:%02d", minutes, seconds)
            binding.tvTimer.text = getString(R.string.timer_format, timeStr)
            timerHandler.postDelayed(this, 1000)
        }
    }

    companion object {
        private const val TAG = "MapActivity"
    }

    // 定位权限请求
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                enableMyLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                enableMyLocation()
            }
            else -> {
                Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 通知权限请求 (Android 13+)
    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "需要通知权限来显示追踪状态", Toast.LENGTH_SHORT).show()
        }
    }

    // Places Autocomplete 启动器
    private val autocompleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleAutocompleteResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Places SDK
        initPlaces()

        // 初始化 Directions API
        DirectionsService.init(this)

        // 初始化定位客户端
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 初始化交通方式检测器
        transportModeDetector = TransportModeDetector(this)

        // 初始化地图
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupUI()
        observeViewModel()
        observeLocationManager()
        observeNavigationManager()
        observeTransportModeDetector()

        // 请求通知权限 (Android 13+)
        requestNotificationPermission()
    }

    /**
     * 请求通知权限
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * 初始化 Places SDK
     */
    private fun initPlaces() {
        if (!Places.isInitialized()) {
            // 从 AndroidManifest.xml 获取 API Key
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
            if (apiKey.isNotEmpty()) {
                Places.initialize(applicationContext, apiKey)
                Log.d(TAG, "Places SDK initialized")
            } else {
                Log.e(TAG, "Google Maps API Key not found")
            }
        }
    }

    /**
     * 设置 UI 事件监听
     */
    private fun setupUI() {
        // 初始化底部区域：显示广告占位，隐藏开始按钮
        binding.cardAdPlaceholder.visibility = View.VISIBLE
        binding.cardBottomPanel.visibility = View.GONE

        // 起点输入框点击
        binding.etOrigin.setOnClickListener {
            isSearchingOrigin = true
            launchPlaceAutocomplete()
        }

        // 终点输入框点击
        binding.etDestination.setOnClickListener {
            isSearchingOrigin = false
            launchPlaceAutocomplete()
        }

        // 交换起点终点按钮
        binding.btnSwap.setOnClickListener {
            swapOriginAndDestination()
        }

        // 交通方式选择监听器
        binding.chipGroupTransport.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            // 检查是否选择了目的地
            if (destinationLatLng == null) {
                Toast.makeText(this, "请先选择目的地", Toast.LENGTH_SHORT).show()
                binding.chipGroupTransport.clearCheck()
                binding.chipWalking.isChecked = true  // 重置为默认
                return@setOnCheckedStateChangeListener
            }

            // 根据选中的 Chip 确定交通方式
            val mode = when (checkedIds.first()) {
                R.id.chipDriving -> TransportMode.DRIVING
                R.id.chipTransit -> TransportMode.BUS
                R.id.chipCycling -> TransportMode.CYCLING
                R.id.chipWalking -> TransportMode.WALKING
                else -> TransportMode.WALKING
            }

            // 调用 ViewModel 获取路线
            viewModel.fetchRouteByMode(mode)
        }

        // 初始化路线步骤 RecyclerView
        binding.rvRouteSteps.apply {
            adapter = routeStepAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MapActivity)
        }

        // 初始化路线选择 RecyclerView（横向滚动）
        binding.rvRouteOptions.apply {
            adapter = routeOptionAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                this@MapActivity,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        // 行程追踪按钮
        binding.btnTracking.setOnClickListener {
            when (viewModel.tripState.value) {
                is TripState.Idle, is TripState.Completed -> {
                    // 检查是否有路线
                    val hasRoute = !viewModel.routePoints.value.isNullOrEmpty()
                    if (!hasRoute) {
                        // 提示用户先获取路线
                        Toast.makeText(
                            this,
                            "提示：请先点击\"低碳路线\"或\"平衡路线\"获取导航路线",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    startLocationTracking()
                    viewModel.startTracking()
                }
                is TripState.Tracking -> {
                    stopLocationTracking()
                    viewModel.stopTracking()
                }
                else -> { /* 忽略其他状态 */ }
            }
        }

        // 定位按钮
        binding.fabMyLocation.setOnClickListener {
            isFollowingUser = true
            moveToCurrentLocation()
            // 重置起点为当前位置
            resetOriginToMyLocation()
        }
    }

    /**
     * 启动位置追踪服务
     */
    private fun startLocationTracking() {
        Log.d(TAG, "Starting location tracking service")
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // 检查是否有路线，如果有则进入导航模式
        val routePoints = viewModel.routePoints.value
        if (!routePoints.isNullOrEmpty()) {
            isNavigationMode = true
            NavigationManager.setRoute(routePoints)
            NavigationManager.startNavigation()

            // 隐藏原始路线，改用导航路线显示
            routePolyline?.remove()
            routePolyline = null

            Log.d(TAG, "Navigation mode started with ${routePoints.size} points")
        } else {
            isNavigationMode = false
            Log.d(TAG, "Track recording mode started (no route)")
        }

        // 清除之前的轨迹
        trackPolyline?.remove()
        trackPolyline = null
        traveledPolyline?.remove()
        traveledPolyline = null
        remainingPolyline?.remove()
        remainingPolyline = null

        // 重置里程碑追踪
        reachedMilestones.clear()

        // 记录导航开始时间
        navigationStartTime = System.currentTimeMillis()
        detectedTransportMode = null

        isFollowingUser = true

        // 启动交通方式检测
        transportModeDetector.startDetection()
        Log.d(TAG, "Transport mode detection started")

        // 检测是否为模拟器（改进版）
        val isEmulator = isRunningOnEmulator()
        Log.d(TAG, "========== Device Detection ==========")
        Log.d(TAG, "isEmulator: $isEmulator")
        Log.d(TAG, "FINGERPRINT: ${Build.FINGERPRINT}")
        Log.d(TAG, "MODEL: ${Build.MODEL}")
        Log.d(TAG, "MANUFACTURER: ${Build.MANUFACTURER}")
        Log.d(TAG, "BRAND: ${Build.BRAND}")
        Log.d(TAG, "DEVICE: ${Build.DEVICE}")
        Log.d(TAG, "PRODUCT: ${Build.PRODUCT}")
        Log.d(TAG, "======================================")

        // 临时强制模拟器模式（用于调试）
        // TODO: 确认设备检测正常后移除这个强制逻辑
        val forceEmulatorMode = true

        // 模拟器测试：10秒后显示模拟检测结果
        if (isEmulator || forceEmulatorMode) {
            Log.w(TAG, "Running on emulator (detected=$isEmulator, forced=$forceEmulatorMode) - will show simulated detection in 10 seconds")
            Handler(Looper.getMainLooper()).postDelayed({
                showEmulatorMockDetection()
            }, 10000) // 10秒后显示模拟结果
        } else {
            Log.d(TAG, "Running on real device - using real sensor detection")
        }

        // 备用机制：10秒后如果还没有检测结果，强制显示提示
        Handler(Looper.getMainLooper()).postDelayed({
            if (binding.tvRouteType.text.toString().contains("正在检测交通方式")) {
                Log.w(TAG, "Detection timeout - forcing fallback message")
                runOnUiThread {
                    binding.tvRouteType.text = "⚠️ 交通方式检测异常\n请查看日志或使用真机测试"
                    Toast.makeText(this, "传感器数据采集失败\n建议使用真机测试", Toast.LENGTH_LONG).show()
                }
            }
        }, 10000) // 10秒后检查

        // 启动计时器
        startTimer()
    }

    /**
     * 启动行程计时器
     */
    private fun startTimer() {
        timerStartTime = SystemClock.elapsedRealtime()
        binding.tvTimer.visibility = View.VISIBLE
        binding.tvTimer.text = getString(R.string.timer_format, "00:00")
        timerHandler.post(timerRunnable)
    }

    /**
     * 停止行程计时器
     */
    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
    }

    /**
     * 隐藏计时器
     */
    private fun hideTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        binding.tvTimer.visibility = View.GONE
    }

    /**
     * 停止位置追踪服务
     */
    private fun stopLocationTracking() {
        Log.d(TAG, "Stopping location tracking service")
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        startService(intent)

        // 停止计时器（保留显示最终用时）
        stopTimer()

        // 保存导航历史记录（如果有有效数据）
        saveNavigationHistory()

        // 停止导航
        if (isNavigationMode) {
            NavigationManager.stopNavigation()
            isNavigationMode = false

            // 清除导航路线
            traveledPolyline?.remove()
            traveledPolyline = null
            remainingPolyline?.remove()
            remainingPolyline = null
        }

        // 停止交通方式检测
        transportModeDetector.stopDetection()
        Log.d(TAG, "Transport mode detection stopped")
    }

    /**
     * 保存导航历史记录
     */
    private fun saveNavigationHistory() {
        // 检查是否有有效的导航数据
        if (navigationStartTime == 0L) {
            Log.w(TAG, "Navigation start time not set, skipping history save")
            return
        }

        val origin = originLatLng ?: viewModel.currentLocation.value
        val destination = destinationLatLng

        if (origin == null || destination == null) {
            Log.w(TAG, "Origin or destination not set, skipping history save")
            return
        }

        // 获取路线数据
        val routePoints = viewModel.routePoints.value ?: emptyList()
        val trackPoints = if (isNavigationMode) {
            NavigationManager.traveledPoints.value ?: emptyList()
        } else {
            LocationManager.trackPoints.value ?: emptyList()
        }

        // 如果没有轨迹点，跳过保存
        if (trackPoints.isEmpty()) {
            Log.w(TAG, "No track points recorded, skipping history save")
            return
        }

        // 获取距离数据
        val totalDistance = viewModel.routePoints.value?.let { points ->
            // 计算路线总距离（如果有规划路线）
            viewModel.recommendedRoute.value?.total_distance?.times(1000) ?: 0.0
        } ?: 0.0

        val traveledDistance = if (isNavigationMode) {
            NavigationManager.traveledDistance.value?.toDouble() ?: 0.0
        } else {
            LocationManager.totalDistance.value?.toDouble() ?: 0.0
        }

        // 获取交通方式
        val transportMode = viewModel.selectedTransportMode.value?.value ?: "walking"

        // 获取环保数据
        val carbonResult = viewModel.carbonResult.value
        val totalCarbon = carbonResult?.total_carbon_emission ?: 0.0
        val carbonSaved = carbonResult?.carbon_saved ?: 0.0
        val isGreenTrip = carbonResult?.is_green_trip ?: (carbonSaved > 0)
        val greenPoints = carbonResult?.green_points ?: 0

        // 获取路线类型
        val routeType = viewModel.recommendedRoute.value?.route_type

        // 在后台线程保存数据
        lifecycleScope.launch {
            try {
                val repository = NavigationHistoryRepository.getInstance()
                val historyId = repository.saveNavigationHistory(
                    tripId = null, // 如果有后端trip_id可以传入
                    userId = null, // 如果有用户系统可以传入用户ID
                    startTime = navigationStartTime,
                    endTime = System.currentTimeMillis(),
                    origin = origin,
                    originName = originName,
                    destination = destination,
                    destinationName = destinationName,
                    routePoints = routePoints,
                    trackPoints = trackPoints,
                    totalDistance = totalDistance,
                    traveledDistance = traveledDistance,
                    transportMode = transportMode,
                    detectedMode = detectedTransportMode,
                    totalCarbon = totalCarbon,
                    carbonSaved = carbonSaved,
                    isGreenTrip = isGreenTrip,
                    greenPoints = greenPoints,
                    routeType = routeType
                )

                Log.d(TAG, "Navigation history saved successfully with ID: $historyId")

                // 可以在这里显示保存成功的提示（可选）
                // runOnUiThread {
                //     Toast.makeText(this@MapActivity, "行程已保存", Toast.LENGTH_SHORT).show()
                // }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save navigation history", e)
            }
        }
    }

    /**
     * 观察 LocationManager 的位置更新
     */
    private fun observeLocationManager() {
        // 观察当前位置
        LocationManager.currentLocation.observe(this) { latLng ->
            // 更新 ViewModel
            viewModel.updateCurrentLocation(latLng)

            // 如果正在追踪且开启了跟随模式，移动相机
            if (LocationManager.isTracking.value == true && isFollowingUser) {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }

            // 更新交通方式检测器的位置（用于 GPS 速度）
            if (LocationManager.isTracking.value == true) {
                val location = android.location.Location("gps").apply {
                    latitude = latLng.latitude
                    longitude = latLng.longitude
                    time = System.currentTimeMillis()
                    // 注意：这里的 speed 需要从实际的 Location 对象获取
                    // 当前使用默认值 0，实际应该从 LocationTrackingService 获取
                }
                transportModeDetector.updateLocation(location)
                Log.d(TAG, "Location updated for detector: lat=${latLng.latitude}, lng=${latLng.longitude}")
            }
        }

        // 观察轨迹点（仅在非导航模式下绘制）
        LocationManager.trackPoints.observe(this) { points ->
            if (points.isNotEmpty() && !isNavigationMode) {
                drawTrackPolyline(points)
            }
        }

        // 观察总距离
        LocationManager.totalDistance.observe(this) { distance ->
            if (LocationManager.isTracking.value == true && !isNavigationMode) {
                updateTrackingInfo(distance)
            }
        }
    }

    /**
     * 观察 NavigationManager 的导航状态
     */
    private fun observeNavigationManager() {
        // 观察已走过的路线
        NavigationManager.traveledPoints.observe(this) { points ->
            if (isNavigationMode && points.isNotEmpty()) {
                drawTraveledRoute(points)
            }
        }

        // 观察剩余路线
        NavigationManager.remainingPoints.observe(this) { points ->
            if (isNavigationMode && points.isNotEmpty()) {
                drawRemainingRoute(points)
            }
        }

        // 观察已行进距离
        NavigationManager.traveledDistance.observe(this) { distance ->
            if (isNavigationMode) {
                updateNavigationInfo(distance)
            }
        }

        // 观察是否到达目的地
        NavigationManager.currentRouteIndex.observe(this) { _ ->
            if (NavigationManager.hasReachedDestination()) {
                onReachedDestination()
            }
        }
    }

    /**
     * 观察交通方式检测器
     */
    private fun observeTransportModeDetector() {
        lifecycleScope.launch {
            Log.d(TAG, "Started observing transport mode detector")
            transportModeDetector.detectedMode.collect { prediction ->
                Log.d(TAG, "Received prediction: $prediction")
                prediction?.let {
                    onTransportModeDetected(it)
                }
            }
        }
    }

    /**
     * 处理检测到的交通方式
     */
    private fun onTransportModeDetected(prediction: com.ecogo.app.ml.TransportModePrediction) {
        if (!LocationManager.isTracking.value!!) return

        // 记录检测到的交通方式（用于保存到历史记录）
        detectedTransportMode = prediction.mode.name.lowercase()

        val modeIcon = when (prediction.mode) {
            com.ecogo.app.ml.TransportModeLabel.WALKING -> "🚶"
            com.ecogo.app.ml.TransportModeLabel.CYCLING -> "🚴"
            com.ecogo.app.ml.TransportModeLabel.BUS -> "🚌"
            com.ecogo.app.ml.TransportModeLabel.SUBWAY -> "🚇"
            com.ecogo.app.ml.TransportModeLabel.DRIVING -> "🚗"
            else -> "❓"
        }

        val modeText = when (prediction.mode) {
            com.ecogo.app.ml.TransportModeLabel.WALKING -> "步行"
            com.ecogo.app.ml.TransportModeLabel.CYCLING -> "骑行"
            com.ecogo.app.ml.TransportModeLabel.BUS -> "公交"
            com.ecogo.app.ml.TransportModeLabel.SUBWAY -> "地铁"
            com.ecogo.app.ml.TransportModeLabel.DRIVING -> "驾车"
            else -> "未知"
        }

        val confidencePercent = (prediction.confidence * 100).toInt()

        // 更新 UI 显示检测到的交通方式（在顶部显著位置）
        runOnUiThread {
            if (binding.cardRouteInfo.visibility == View.VISIBLE) {
                // 在路线类型位置显示当前交通方式
                if (isNavigationMode) {
                    binding.tvRouteType.text = "$modeIcon 当前交通: $modeText ($confidencePercent%)"
                } else {
                    binding.tvRouteType.text = "$modeIcon 检测到: $modeText ($confidencePercent%)"
                }
            }
        }

        Log.d(TAG, "检测到交通方式: $modeText, 置信度: ${prediction.confidence}")
    }

    /**
     * 检测是否运行在模拟器上
     * 检查多个设备属性以提高可靠性
     */
    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * 模拟器模拟检测结果（仅用于 UI 测试）
     */
    private fun showEmulatorMockDetection() {
        Log.d(TAG, "showEmulatorMockDetection() called")
        Log.d(TAG, "LocationManager.isTracking.value = ${LocationManager.isTracking.value}")

        val isTracking = LocationManager.isTracking.value ?: false
        if (!isTracking) {
            Log.w(TAG, "Cannot show mock detection - tracking is not active")
            return
        }

        runOnUiThread {
            Log.d(TAG, "cardRouteInfo.visibility = ${binding.cardRouteInfo.visibility}")

            if (binding.cardRouteInfo.visibility == View.VISIBLE) {
                binding.tvRouteType.text = "🚶 模拟检测: 步行 (模拟器测试)"
                Log.w(TAG, "Showing emulator mock detection (real sensors not available)")
                Toast.makeText(
                    this,
                    "⚠️ 模拟器无真实传感器\n显示模拟结果\n请用真机测试实际检测功能",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Log.w(TAG, "Cannot show mock detection - cardRouteInfo is not visible")
            }
        }
    }

    /**
     * 绘制已走过的路线（灰色）
     */
    private fun drawTraveledRoute(points: List<LatLng>) {
        traveledPolyline?.remove()

        if (points.size < 2) return

        traveledPolyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(ContextCompat.getColor(this, R.color.route_traveled))
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * 绘制剩余路线（蓝色）
     */
    private fun drawRemainingRoute(points: List<LatLng>) {
        remainingPolyline?.remove()

        if (points.size < 2) return

        remainingPolyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(ContextCompat.getColor(this, R.color.route_remaining))
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * 更新导航信息显示
     */
    private fun updateNavigationInfo(traveledMeters: Float) {
        val traveledKm = traveledMeters / 1000f
        val remainingMeters = NavigationManager.remainingDistance.value ?: 0f
        val remainingKm = remainingMeters / 1000f

        if (binding.cardRouteInfo.visibility == View.VISIBLE) {
            // 获取实时碳排放信息和鼓励消息
            val encouragementMessage = generateEncouragementMessage(traveledMeters)
            binding.tvCarbonSaved.text = encouragementMessage
            binding.tvDuration.text = String.format("剩余: %.2f 公里", remainingKm)

            // 检查是否到达里程碑
            checkMilestones(traveledMeters)
        }
    }

    /**
     * 计算实时碳排放减少量（单位：克）
     */
    private fun calculateRealTimeCarbonSaved(distanceMeters: Float): Double {
        val distanceKm = distanceMeters / 1000.0
        val mode = viewModel.selectedTransportMode.value

        // 碳排放因子 (kg CO2 / km)
        val emissionFactor = when (mode) {
            TransportMode.WALKING, TransportMode.CYCLING -> 0.0
            TransportMode.BUS, TransportMode.SUBWAY -> 0.05
            else -> 0.15  // DRIVING 或其他
        }

        val currentModeCarbon = distanceKm * emissionFactor
        val drivingCarbon = distanceKm * 0.15  // 与驾车对比
        val carbonSaved = (drivingCarbon - currentModeCarbon) * 1000  // 转为克

        return carbonSaved.coerceAtLeast(0.0)
    }

    /**
     * 生成鼓励消息
     */
    private fun generateEncouragementMessage(distanceMeters: Float): String {
        val mode = viewModel.selectedTransportMode.value
        val carbonSavedGrams = calculateRealTimeCarbonSaved(distanceMeters)

        return when (mode) {
            TransportMode.WALKING, TransportMode.CYCLING -> {
                // 步行/骑行：显示减碳量和鼓励
                if (carbonSavedGrams >= 1) {
                    String.format("已减碳 %.0f g | 继续加油 💪", carbonSavedGrams)
                } else {
                    "绿色出行 | 继续加油 💪"
                }
            }
            TransportMode.BUS, TransportMode.SUBWAY -> {
                // 公交/地铁：显示绿色出行进行中
                if (carbonSavedGrams >= 1) {
                    String.format("绿色出行进行中 🚌 | 已减碳 %.0f g", carbonSavedGrams)
                } else {
                    "绿色出行进行中 🚌"
                }
            }
            else -> {
                // 驾车或其他：只显示距离
                String.format("已行进: %.2f 公里", distanceMeters / 1000f)
            }
        }
    }

    /**
     * 检查并显示里程碑
     */
    private fun checkMilestones(distanceMeters: Float) {
        for (milestone in milestones) {
            if (distanceMeters >= milestone && !reachedMilestones.contains(milestone)) {
                reachedMilestones.add(milestone)
                showMilestoneToast(milestone)
                break  // 每次只显示一个里程碑
            }
        }
    }

    /**
     * 显示里程碑Toast
     */
    private fun showMilestoneToast(milestoneMeters: Float) {
        val mode = viewModel.selectedTransportMode.value
        val carbonSavedGrams = calculateRealTimeCarbonSaved(milestoneMeters)

        val message = when (mode) {
            TransportMode.WALKING -> {
                String.format("恭喜！您已步行 %.0f 米，减碳 %.0f g 🎉", milestoneMeters, carbonSavedGrams)
            }
            TransportMode.CYCLING -> {
                String.format("恭喜！您已骑行 %.0f 米，减碳 %.0f g 🚴", milestoneMeters, carbonSavedGrams)
            }
            TransportMode.BUS, TransportMode.SUBWAY -> {
                String.format("恭喜！您已出行 %.0f 米，减碳 %.0f g 🌱", milestoneMeters, carbonSavedGrams)
            }
            else -> {
                String.format("恭喜！您已出行 %.0f 米", milestoneMeters)
            }
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * 到达目的地
     */
    private fun onReachedDestination() {
        Toast.makeText(this, "您已到达目的地！", Toast.LENGTH_LONG).show()
        // 自动停止行程
        stopLocationTracking()
        viewModel.stopTracking()
    }

    /**
     * 绘制实时轨迹
     */
    private fun drawTrackPolyline(points: List<LatLng>) {
        trackPolyline?.remove()

        if (points.size < 2) return

        trackPolyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(10f)
                .color(ContextCompat.getColor(this, R.color.green_primary))
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * 更新追踪信息显示
     */
    private fun updateTrackingInfo(distanceMeters: Float) {
        val distanceKm = distanceMeters / 1000f
        // 可以在路线信息卡片显示实时距离
        if (binding.cardRouteInfo.visibility == View.VISIBLE) {
            // 使用与导航相同的鼓励消息
            val encouragementMessage = generateEncouragementMessage(distanceMeters)
            binding.tvCarbonSaved.text = encouragementMessage

            // 检查是否到达里程碑
            checkMilestones(distanceMeters)
        }
    }

    /**
     * 启动 Places Autocomplete
     */
    private fun launchPlaceAutocomplete() {
        try {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )

            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this)

            autocompleteLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching autocomplete: ${e.message}")
            Toast.makeText(this, "搜索服务暂不可用", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 处理 Autocomplete 返回结果
     */
    private fun handleAutocompleteResult(result: ActivityResult) {
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { data ->
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val latLng = place.latLng

                    if (latLng != null) {
                        if (isSearchingOrigin) {
                            // 设置起点
                            originLatLng = latLng
                            originName = place.name ?: place.address ?: "起点"
                            binding.etOrigin.setText(originName)
                            updateOriginMarker(latLng, originName)
                            viewModel.setOrigin(latLng)  // 使用 setOrigin 而不是 updateCurrentLocation

                            // 如果终点已设置，自动获取默认路线（驾车）
                            if (destinationLatLng != null) {
                                binding.cardTransportModes.visibility = View.VISIBLE
                                binding.chipDriving.isChecked = true
                                viewModel.fetchRouteByMode(TransportMode.DRIVING)
                            }
                        } else {
                            // 设置终点
                            destinationLatLng = latLng
                            destinationName = place.name ?: place.address ?: "目的地"
                            binding.etDestination.setText(destinationName)
                            updateDestinationMarker(latLng, destinationName)
                            viewModel.setDestination(latLng)

                            // 显示交通方式选择卡片
                            binding.cardTransportModes.visibility = View.VISIBLE

                            // 自动获取默认路线（驾车）
                            if (originLatLng != null || viewModel.currentLocation.value != null) {
                                binding.chipDriving.isChecked = true
                                viewModel.fetchRouteByMode(TransportMode.DRIVING)
                            }
                        }

                        // 更新开始按钮可见性
                        updateStartButtonVisibility()

                        // 移动相机到选择的位置
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        // 如果起点和终点都已设置，调整相机显示两点
                        fitBoundsIfReady()
                    }
                }
            }
            AutocompleteActivity.RESULT_ERROR -> {
                result.data?.let { data ->
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.e(TAG, "Autocomplete error: ${status.statusMessage}")
                    Toast.makeText(this, "搜索出错: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Autocomplete canceled")
            }
        }
    }

    /**
     * 交换起点和终点
     */
    private fun swapOriginAndDestination() {
        // 交换位置
        val tempLatLng = originLatLng
        val tempName = originName

        originLatLng = destinationLatLng
        originName = destinationName

        destinationLatLng = tempLatLng
        destinationName = tempName

        // 更新 UI
        binding.etOrigin.setText(if (originLatLng != null) originName else "我的位置")
        binding.etDestination.setText(destinationName)

        // 更新标记
        originLatLng?.let {
            updateOriginMarker(it, originName)
            viewModel.setOrigin(it)  // 交换后更新起点
        }
        destinationLatLng?.let {
            updateDestinationMarker(it, destinationName)
            viewModel.setDestination(it)
        }

        // 清除路线
        routePolyline?.remove()
        routePolyline = null
        binding.cardRouteInfo.visibility = View.GONE

        // 自动获取默认驾车路线（如果起点和终点都已设置）
        if (originLatLng != null && destinationLatLng != null) {
            binding.chipDriving.isChecked = true
            viewModel.fetchRouteByMode(TransportMode.DRIVING)
        }

        // 更新开始按钮可见性
        updateStartButtonVisibility()
    }

    /**
     * 重置起点为当前位置
     */
    @SuppressLint("MissingPermission")
    private fun resetOriginToMyLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                originLatLng = latLng
                originName = "我的位置"
                binding.etOrigin.setText(originName)
                originMarker?.remove()
                originMarker = null
                viewModel.setOrigin(latLng)  // 重置起点为当前位置
                updateStartButtonVisibility()  // 更新开始按钮可见性
            }
        }
    }

    /**
     * 如果起点和终点都设置了，调整相机显示两点
     */
    private fun fitBoundsIfReady() {
        val origin = originLatLng ?: viewModel.currentLocation.value
        val destination = destinationLatLng

        if (origin != null && destination != null) {
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(origin)
            boundsBuilder.include(destination)
            val bounds = boundsBuilder.build()
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        }
    }

    /**
     * 更新开始行程按钮的可见性
     * 只有当起点和终点都设置后才显示按钮，同时隐藏广告占位
     */
    private fun updateStartButtonVisibility() {
        val hasOrigin = originLatLng != null || viewModel.currentLocation.value != null
        val hasDestination = destinationLatLng != null

        if (hasOrigin && hasDestination) {
            // 选择了起点和终点：隐藏广告，显示按钮
            binding.cardAdPlaceholder.visibility = View.GONE
            binding.cardBottomPanel.visibility = View.VISIBLE
        } else {
            // 未选择完：显示广告，隐藏按钮
            binding.cardAdPlaceholder.visibility = View.VISIBLE
            binding.cardBottomPanel.visibility = View.GONE
        }
    }

    /**
     * 观察 ViewModel 数据变化
     */
    private fun observeViewModel() {
        // 观察当前位置
        viewModel.currentLocation.observe(this) { location ->
            // 如果起点是"我的位置"，更新起点
            if (originName == "我的位置" && originLatLng == null) {
                originLatLng = location
            }
        }

        // 观察目的地
        viewModel.destination.observe(this) { destination ->
            destination?.let {
                destinationLatLng = it
            }
        }

        // 观察行程状态
        viewModel.tripState.observe(this) { state ->
            updateTrackingUI(state)
        }

        // 观察推荐路线
        viewModel.recommendedRoute.observe(this) { route ->
            route?.let { updateRouteInfo(it) }
        }

        // 观察路线点
        viewModel.routePoints.observe(this) { points ->
            drawRoute(points)
        }

        // 观察碳足迹结果
        viewModel.carbonResult.observe(this) { result ->
            result?.let {
                val carbonSavedStr = String.format("%.2f", it.carbon_saved)

                // 记录绿色出行统计（如果有减碳）
                if (it.carbon_saved > 0) {
                    com.ecogo.app.util.GreenTravelStats.recordGreenTrip(this, it.carbon_saved)
                }

                // 显示完成消息
                val message = if (it.is_green_trip) {
                    "🎉 绿色出行完成！减碳 $carbonSavedStr kg，获得 ${it.green_points} 积分"
                } else {
                    "行程完成，碳排放 $carbonSavedStr kg"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // 更新累计统计显示
                binding.tvCumulativeImpact.text = com.ecogo.app.util.GreenTravelStats.formatWeeklyImpact(this)
            }
        }

        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 观察错误消息
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // 观察成功消息
        viewModel.successMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }
    }

    /**
     * 地图准备就绪回调
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 配置地图
        map.apply {
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = false

            // 地图点击也可以设置目的地
            setOnMapClickListener { latLng ->
                // 如果正在追踪，禁止修改目的地
                if (viewModel.tripState.value is TripState.Tracking) {
                    return@setOnMapClickListener
                }

                // 显示确认对话框
                androidx.appcompat.app.AlertDialog.Builder(this@MapActivity)
                    .setTitle("设置目的地")
                    .setMessage("是否将此位置设置为目的地？")
                    .setPositiveButton("确定") { dialog, _ ->
                        destinationLatLng = latLng
                        destinationName = "地图上的位置"
                        binding.etDestination.setText(destinationName)
                        updateDestinationMarker(latLng, destinationName)
                        viewModel.setDestination(latLng)

                        // 显示交通方式选择卡片
                        binding.cardTransportModes.visibility = View.VISIBLE

                        // 自动获取默认路线（驾车）
                        if (originLatLng != null || viewModel.currentLocation.value != null) {
                            binding.chipDriving.isChecked = true
                            viewModel.fetchRouteByMode(TransportMode.DRIVING)
                        }

                        // 更新开始按钮可见性
                        updateStartButtonVisibility()

                        fitBoundsIfReady()
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            // 长按清除目的地
            setOnMapLongClickListener {
                if (viewModel.tripState.value !is TripState.Tracking) {
                    clearDestination()
                }
            }

            // 地图移动时停止跟随
            setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    isFollowingUser = false
                }
            }
        }

        // 请求定位权限
        checkLocationPermission()
    }

    /**
     * 清除目的地
     */
    private fun clearDestination() {
        destinationLatLng = null
        destinationName = ""
        binding.etDestination.setText("")
        destinationMarker?.remove()
        destinationMarker = null
        routePolyline?.remove()
        routePolyline = null
        binding.cardRouteInfo.visibility = View.GONE
        binding.cardTransportModes.visibility = View.GONE
        // 隐藏按钮，显示广告占位
        binding.cardBottomPanel.visibility = View.GONE
        binding.cardAdPlaceholder.visibility = View.VISIBLE
        viewModel.clearDestination()
    }

    /**
     * 检查定位权限
     */
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    /**
     * 启用我的位置图层
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false

        // 获取当前位置
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                originLatLng = latLng
                viewModel.updateCurrentLocation(latLng)
                moveToCurrentLocation()
                updateStartButtonVisibility()  // 更新开始按钮可见性
            }
        }
    }

    /**
     * 移动相机到当前位置
     */
    private fun moveToCurrentLocation() {
        val location = LocationManager.currentLocation.value
            ?: originLatLng
            ?: viewModel.currentLocation.value

        location?.let {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }

    /**
     * 更新起点标记
     */
    private fun updateOriginMarker(location: LatLng, title: String) {
        originMarker?.remove()
        originMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    /**
     * 更新目的地标记
     */
    private fun updateDestinationMarker(location: LatLng, title: String) {
        destinationMarker?.remove()
        destinationMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    /**
     * 绘制路线（推荐路线预览，蓝色）
     */
    private fun drawRoute(points: List<LatLng>) {
        // 清除之前的所有路线相关的 Polyline
        routePolyline?.remove()
        traveledPolyline?.remove()
        remainingPolyline?.remove()
        trackPolyline?.remove()

        // 重置引用
        routePolyline = null
        traveledPolyline = null
        remainingPolyline = null
        trackPolyline = null

        if (points.isEmpty()) return

        // 使用蓝色显示推荐路线（与百度/谷歌地图一致）
        routePolyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(ContextCompat.getColor(this, R.color.route_remaining))
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )

        // 调整相机显示完整路线
        if (points.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            points.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        }
    }

    /**
     * 更新路线信息卡片
     */
    private fun updateRouteInfo(route: com.ecogo.app.data.model.RouteRecommendData) {
        binding.cardRouteInfo.visibility = View.VISIBLE

        // 路线类型
        val routeTypeText = when (route.route_type) {
            "low_carbon" -> "低碳路线"
            "balanced" -> "平衡路线"
            else -> "推荐路线"
        }
        binding.tvRouteType.text = routeTypeText

        // 碳减排 - 绿色出行强化显示
        val carbonSavedText = if (route.carbon_saved > 0) {
            String.format("🌍 比驾车减少 %.2f kg 碳排放", route.carbon_saved)
        } else {
            String.format("碳排放: %.2f kg", route.total_carbon)
        }
        binding.tvCarbonSaved.text = carbonSavedText

        // 根据碳排放设置颜色编码（绿色=低碳，黄色=中碳，红色=高碳）
        val carbonColor = when {
            route.total_carbon == 0.0 -> android.graphics.Color.parseColor("#4CAF50") // 绿色 - 零碳
            route.total_carbon < 0.5 -> android.graphics.Color.parseColor("#8BC34A") // 浅绿 - 低碳
            route.total_carbon < 1.5 -> android.graphics.Color.parseColor("#FFC107") // 黄色 - 中碳
            else -> android.graphics.Color.parseColor("#FF5722") // 红色 - 高碳
        }
        binding.tvCarbonSaved.setTextColor(carbonColor)

        // 环保评级（星级）
        val ecoRating = calculateEcoRating(route.total_carbon, route.total_distance)
        val ratingText = "环保指数: $ecoRating"
        binding.tvRouteType.text = "$routeTypeText  $ratingText"

        // 预计时间 (使用新字段 estimated_duration，兼容旧字段 duration)
        val durationMinutes = route.estimated_duration.takeIf { it > 0 } ?: route.duration ?: 0
        val durationText = "预计: $durationMinutes 分钟"
        binding.tvDuration.text = durationText

        // 显示累计环保贡献（仅绿色出行方式显示）
        if (route.carbon_saved > 0) {
            binding.tvCumulativeImpact.visibility = View.VISIBLE
            binding.tvCumulativeImpact.text = com.ecogo.app.util.GreenTravelStats.formatWeeklyImpact(this)
        } else {
            binding.tvCumulativeImpact.visibility = View.GONE
        }

        // 显示路线选择列表（仅公交模式且有多条路线）
        if (!route.route_alternatives.isNullOrEmpty()) {
            binding.rvRouteOptions.visibility = View.VISIBLE
            routeOptionAdapter.setRoutes(route.route_alternatives)
        } else {
            binding.rvRouteOptions.visibility = View.GONE
        }

        // 显示详细步骤列表（仅公交模式显示详细步骤）
        val hasTransitSteps = route.route_steps?.any { it.travel_mode == "TRANSIT" } == true
        if (hasTransitSteps && !route.route_steps.isNullOrEmpty()) {
            binding.rvRouteSteps.visibility = View.VISIBLE
            routeStepAdapter.setSteps(route.route_steps)
        } else {
            binding.rvRouteSteps.visibility = View.GONE
        }
    }

    /**
     * 计算环保评级（星级）
     * 基于碳排放量和距离计算环保指数
     */
    private fun calculateEcoRating(totalCarbon: Double, distance: Double): String {
        // 计算每公里碳排放
        val carbonPerKm = if (distance > 0) totalCarbon / distance else totalCarbon

        // 根据每公里碳排放计算星级（0-5星）
        val stars = when {
            carbonPerKm == 0.0 -> "⭐⭐⭐⭐⭐" // 零碳 - 5星
            carbonPerKm < 0.03 -> "⭐⭐⭐⭐" // 地铁级别 - 4星
            carbonPerKm < 0.06 -> "⭐⭐⭐" // 公交级别 - 3星
            carbonPerKm < 0.10 -> "⭐⭐" // 混合出行 - 2星
            else -> "⭐" // 高碳 - 1星
        }

        return stars
    }

    /**
     * 处理用户选择路线
     */
    private fun onRouteSelected(route: com.ecogo.app.data.model.RouteAlternative) {
        Log.d(TAG, "Route selected: ${route.summary}")

        // 更新地图上的路线
        val points = route.route_points.map { com.google.android.gms.maps.model.LatLng(it.lat, it.lng) }
        drawRoute(points)

        // 更新路线信息
        binding.tvCarbonSaved.text = String.format("减碳: %.2f kg", route.total_carbon)
        binding.tvDuration.text = "预计: ${route.estimated_duration} 分钟"

        // 更新详细步骤
        if (route.route_steps.any { it.travel_mode == "TRANSIT" }) {
            binding.rvRouteSteps.visibility = View.VISIBLE
            routeStepAdapter.setSteps(route.route_steps)
        } else {
            binding.rvRouteSteps.visibility = View.GONE
        }

        Toast.makeText(this, "已切换到: ${route.summary}", Toast.LENGTH_SHORT).show()
    }

    /**
     * 更新行程追踪 UI
     */
    private fun updateTrackingUI(state: TripState) {
        when (state) {
            is TripState.Idle -> {
                binding.btnTracking.text = getString(R.string.start_tracking)
                binding.btnTracking.isEnabled = true
                binding.chipGroupTransport.visibility = View.VISIBLE
                binding.cardSearch.visibility = View.VISIBLE
                hideTimer()
                // 清除追踪轨迹
                trackPolyline?.remove()
                trackPolyline = null
            }
            is TripState.Starting -> {
                binding.btnTracking.text = "正在开始..."
                binding.btnTracking.isEnabled = false
            }
            is TripState.Tracking -> {
                binding.btnTracking.text = getString(R.string.stop_tracking)
                binding.btnTracking.isEnabled = true
                binding.chipGroupTransport.visibility = View.GONE
                binding.cardSearch.visibility = View.GONE
                // 显示追踪信息卡片
                binding.cardRouteInfo.visibility = View.VISIBLE

                // 显示正在检测交通方式
                binding.tvRouteType.text = "🔄 正在检测交通方式..."

                if (isNavigationMode) {
                    // 导航模式
                    binding.tvCarbonSaved.text = "已行进: 0.00 公里"
                    val remainingKm = (NavigationManager.remainingDistance.value ?: 0f) / 1000f
                    binding.tvDuration.text = String.format("剩余: %.2f 公里", remainingKm)
                } else {
                    // 纯轨迹记录模式
                    binding.tvCarbonSaved.text = "已行进: 0.00 公里"
                    binding.tvDuration.text = "实时记录GPS轨迹"
                }
            }
            is TripState.Stopping -> {
                binding.btnTracking.text = "正在结束..."
                binding.btnTracking.isEnabled = false
            }
            is TripState.Completed -> {
                binding.btnTracking.text = getString(R.string.start_tracking)
                binding.btnTracking.isEnabled = true
                binding.chipGroupTransport.visibility = View.VISIBLE
                binding.cardSearch.visibility = View.VISIBLE
                binding.cardRouteInfo.visibility = View.GONE
                hideTimer()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清除计时器，防止内存泄漏
        timerHandler.removeCallbacks(timerRunnable)
        // 如果 Activity 销毁时还在追踪，停止服务
        if (LocationManager.isTracking.value == true) {
            stopLocationTracking()
        }
        // 清除导航状态
        if (NavigationManager.isNavigating.value == true) {
            NavigationManager.clearNavigation()
        }
        // 清除交通方式检测器
        transportModeDetector.cleanup()
    }
}
