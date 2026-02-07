package com.ecogo.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ecogo.app.ui.map.MapActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

/**
 * 前台定位服务
 * 用于在行程追踪期间持续获取用户位置
 */
class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var isTracking = false

    companion object {
        private const val TAG = "LocationTrackingService"

        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.ecogo.app.action.START_TRACKING"
        const val ACTION_STOP = "com.ecogo.app.action.STOP_TRACKING"

        // 位置更新间隔
        private const val UPDATE_INTERVAL_MS = 3000L  // 3 秒
        private const val FASTEST_INTERVAL_MS = 2000L // 最快 2 秒
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "行程追踪",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示行程追踪状态"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台通知
     */
    private fun createNotification(distance: Float = 0f): Notification {
        val intent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceText = if (distance > 0) {
            String.format("已行进 %.2f 公里", distance / 1000)
        } else {
            "正在记录您的绿色出行轨迹"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EcoGo 行程追踪中")
            .setContentText(distanceText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * 更新通知
     */
    private fun updateNotification(distance: Float) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(distance))
    }

    /**
     * 设置位置回调
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")

                    val latLng = LatLng(location.latitude, location.longitude)

                    // 更新 LocationManager（轨迹记录）
                    LocationManager.updateLocation(location.latitude, location.longitude)

                    // 更新 NavigationManager（导航路线匹配）
                    if (NavigationManager.isNavigating.value == true) {
                        NavigationManager.updateLocation(latLng)
                    }

                    // 更新通知显示距离
                    val distance = if (NavigationManager.isNavigating.value == true) {
                        NavigationManager.traveledDistance.value ?: 0f
                    } else {
                        LocationManager.totalDistance.value ?: 0f
                    }
                    if (distance > 0) {
                        updateNotification(distance)
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                Log.d(TAG, "Location availability: ${availability.isLocationAvailable}")
            }
        }
    }

    /**
     * 开始追踪
     */
    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (isTracking) {
            Log.d(TAG, "Already tracking")
            return
        }

        Log.d(TAG, "Starting tracking")
        isTracking = true

        // 通知 LocationManager 开始追踪
        LocationManager.startTracking()

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())

        // 配置高精度位置请求
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            setWaitForAccurateLocation(true)
            setMinUpdateDistanceMeters(5f)  // 最小移动 5 米才更新
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * 停止追踪
     */
    private fun stopTracking() {
        if (!isTracking) {
            Log.d(TAG, "Not tracking")
            return
        }

        Log.d(TAG, "Stopping tracking")
        isTracking = false

        // 通知 LocationManager 停止追踪
        LocationManager.stopTracking()

        // 停止位置更新
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // 停止前台服务
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            LocationManager.stopTracking()
        }
    }
}
