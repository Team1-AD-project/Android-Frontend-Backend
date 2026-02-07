package com.ecogo.app.ml

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 传感器数据采集器
 * 负责采集加速度计、陀螺仪、GPS 和气压计数据
 */
class SensorDataCollector(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 传感器
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    // 当前传感器读数
    private var currentAccX = 0f
    private var currentAccY = 0f
    private var currentAccZ = 0f
    private var currentGyroX = 0f
    private var currentGyroY = 0f
    private var currentGyroZ = 0f
    private var currentPressure = 1013.25f  // 标准气压
    private var currentGpsSpeed = 0f

    // 数据缓冲区（用于存储 5 秒窗口的数据）
    private val dataBuffer = ConcurrentLinkedQueue<SensorRawData>()

    // 窗口参数
    private val windowSizeMs = 5000L  // 5 秒窗口
    private val samplingIntervalMs = 50L  // 20 Hz 采样频率
    private val slidingStepMs = 2500L  // 2.5 秒滑动步长

    // 协程
    private var collectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 数据窗口流（外部可以订阅）
    private val _windowFlow = MutableStateFlow<SensorWindow?>(null)
    val windowFlow: StateFlow<SensorWindow?> = _windowFlow

    // 是否正在采集
    private var isCollecting = false

    companion object {
        private const val TAG = "SensorDataCollector"
    }

    /**
     * 开始采集传感器数据
     */
    fun startCollecting() {
        if (isCollecting) {
            Log.w(TAG, "Already collecting data")
            return
        }

        Log.d(TAG, "Starting sensor data collection")
        isCollecting = true

        // 注册传感器监听器
        accelerometer?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_GAME  // ~20ms = 50Hz
            )
        } ?: Log.w(TAG, "Accelerometer not available")

        gyroscope?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_GAME
            )
        } ?: Log.w(TAG, "Gyroscope not available")

        pressure?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } ?: Log.w(TAG, "Pressure sensor not available")

        // 启动数据采样协程
        collectJob = scope.launch {
            while (isActive && isCollecting) {
                // 每 50ms 记录一次当前传感器读数
                val data = SensorRawData(
                    timestamp = System.currentTimeMillis(),
                    accelerometerX = currentAccX,
                    accelerometerY = currentAccY,
                    accelerometerZ = currentAccZ,
                    gyroscopeX = currentGyroX,
                    gyroscopeY = currentGyroY,
                    gyroscopeZ = currentGyroZ,
                    gpsSpeed = currentGpsSpeed,
                    pressure = currentPressure
                )
                dataBuffer.offer(data)

                // 清理旧数据（保留最近 5 秒）
                val now = System.currentTimeMillis()
                while (dataBuffer.peek()?.timestamp?.let { now - it > windowSizeMs } == true) {
                    dataBuffer.poll()
                }

                // 每 2.5 秒生成一个窗口
                if (dataBuffer.size >= windowSizeMs / samplingIntervalMs) {
                    generateWindow()
                }

                delay(samplingIntervalMs)
            }
        }
    }

    /**
     * 停止采集传感器数据
     */
    fun stopCollecting() {
        if (!isCollecting) return

        Log.d(TAG, "Stopping sensor data collection")
        isCollecting = false

        // 取消注册传感器
        sensorManager.unregisterListener(this)

        // 取消协程
        collectJob?.cancel()
        collectJob = null

        // 清空缓冲区
        dataBuffer.clear()
    }

    /**
     * 更新 GPS 速度（从外部 LocationManager 调用）
     */
    fun updateGpsSpeed(location: Location) {
        currentGpsSpeed = location.speed  // m/s
    }

    /**
     * 生成数据窗口
     */
    private fun generateWindow() {
        val windowData = dataBuffer.toList()
        if (windowData.isEmpty()) return

        val window = SensorWindow(
            startTime = windowData.first().timestamp,
            endTime = windowData.last().timestamp,
            data = windowData
        )

        Log.d(TAG, "Generated window with ${windowData.size} samples")
        _windowFlow.value = window
    }

    /**
     * 传感器事件回调
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                currentAccX = event.values[0]
                currentAccY = event.values[1]
                currentAccZ = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                currentGyroX = event.values[0]
                currentGyroY = event.values[1]
                currentGyroZ = event.values[2]
            }
            Sensor.TYPE_PRESSURE -> {
                currentPressure = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要处理精度变化
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        stopCollecting()
        scope.cancel()
    }

    /**
     * 保存数据窗口到本地（用于训练数据收集）
     */
    fun saveWindowForTraining(window: SensorWindow, label: TransportModeLabel) {
        // TODO: 将窗口数据保存到本地文件或数据库
        // 格式：CSV 或 JSON
        // 用于后续训练 Random Forest 模型
        Log.d(TAG, "Saving window with label: $label")
    }
}
