package com.ecogo.app.ml

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 交通方式检测器集成示例
 * 展示如何在 MapActivity 中使用传感器采集和交通方式检测
 */
class TransportModeDetectorIntegration(
    private val context: Context,
    private val onModeDetected: (TransportModePrediction) -> Unit
) : LifecycleObserver {

    private val detector = TransportModeDetector(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "TMDetectorIntegration"
    }

    /**
     * 开始检测（在导航开始时调用）
     */
    fun start() {
        Log.d(TAG, "Starting transport mode detection")
        detector.startDetection()

        // 监听检测结果
        scope.launch {
            detector.detectedMode.collect { prediction ->
                prediction?.let {
                    Log.d(TAG, "Mode detected: ${it.mode} (${it.confidence})")
                    onModeDetected(it)
                }
            }
        }
    }

    /**
     * 停止检测（在导航结束时调用）
     */
    fun stop() {
        Log.d(TAG, "Stopping transport mode detection")
        detector.stopDetection()
    }

    /**
     * 更新位置（在 LocationManager 更新时调用）
     */
    fun updateLocation(location: Location) {
        detector.updateLocation(location)
    }

    /**
     * 清理资源
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanup() {
        detector.cleanup()
    }
}

/**
 * 在 MapActivity 中的使用示例：
 *
 * ```kotlin
 * class MapActivity : AppCompatActivity() {
 *
 *     private lateinit var modeDetector: TransportModeDetectorIntegration
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // 初始化交通方式检测器
 *         modeDetector = TransportModeDetectorIntegration(this) { prediction ->
 *             // 检测到交通方式改变
 *             handleModeChange(prediction)
 *         }
 *
 *         // 将检测器绑定到生命周期
 *         lifecycle.addObserver(modeDetector)
 *     }
 *
 *     private fun startLocationTracking() {
 *         // ... 原有的定位逻辑 ...
 *
 *         // 启动交通方式检测
 *         modeDetector.start()
 *     }
 *
 *     private fun stopLocationTracking() {
 *         // ... 原有的定位逻辑 ...
 *
 *         // 停止交通方式检测
 *         modeDetector.stop()
 *     }
 *
 *     private fun onLocationChanged(location: Location) {
 *         // ... 原有的位置更新逻辑 ...
 *
 *         // 更新检测器的位置（用于 GPS 速度）
 *         modeDetector.updateLocation(location)
 *     }
 *
 *     private fun handleModeChange(prediction: TransportModePrediction) {
 *         val currentMode = viewModel.selectedTransportMode.value
 *         val detectedMode = prediction.mode
 *
 *         // 检查是否与用户选择的交通方式不符
 *         if (isModeMismatch(currentMode, detectedMode) && prediction.confidence > 0.7f) {
 *             // 弹窗询问用户是否切换
 *             showModeSwitchDialog(detectedMode)
 *         }
 *
 *         // 更新实时碳排放计算（使用检测到的交通方式）
 *         updateRealTimeCarbonCalculation(detectedMode)
 *     }
 *
 *     private fun isModeMismatch(
 *         userSelected: TransportMode?,
 *         detected: TransportModeLabel
 *     ): Boolean {
 *         // 判断是否明显不符
 *         return when {
 *             userSelected == TransportMode.WALKING && detected == TransportModeLabel.DRIVING -> true
 *             userSelected == TransportMode.CYCLING && detected == TransportModeLabel.BUS -> true
 *             userSelected == TransportMode.DRIVING && detected == TransportModeLabel.WALKING -> true
 *             else -> false
 *         }
 *     }
 *
 *     private fun showModeSwitchDialog(detectedMode: TransportModeLabel) {
 *         AlertDialog.Builder(this)
 *             .setTitle("检测到交通方式变化")
 *             .setMessage("系统检测到您可能在使用${detectedMode.displayName()}，是否切换？")
 *             .setPositiveButton("切换") { _, _ ->
 *                 switchToDetectedMode(detectedMode)
 *             }
 *             .setNegativeButton("保持当前", null)
 *             .show()
 *     }
 *
 *     private fun switchToDetectedMode(mode: TransportModeLabel) {
 *         // 更新 ViewModel 中的交通方式
 *         val transportMode = when (mode) {
 *             TransportModeLabel.WALKING -> TransportMode.WALKING
 *             TransportModeLabel.CYCLING -> TransportMode.CYCLING
 *             TransportModeLabel.BUS -> TransportMode.BUS
 *             TransportModeLabel.DRIVING -> TransportMode.DRIVING
 *             else -> return
 *         }
 *         viewModel.updateSelectedTransportMode(transportMode)
 *     }
 * }
 *
 * // 辅助扩展函数
 * fun TransportModeLabel.displayName(): String {
 *     return when (this) {
 *         TransportModeLabel.WALKING -> "步行"
 *         TransportModeLabel.CYCLING -> "骑行"
 *         TransportModeLabel.BUS -> "公交"
 *         TransportModeLabel.SUBWAY -> "地铁"
 *         TransportModeLabel.DRIVING -> "驾车"
 *         TransportModeLabel.UNKNOWN -> "未知"
 *     }
 * }
 * ```
 */
