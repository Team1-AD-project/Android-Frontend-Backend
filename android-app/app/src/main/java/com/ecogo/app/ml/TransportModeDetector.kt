package com.ecogo.app.ml

import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.LinkedList

/**
 * 交通方式检测器
 * 整合传感器数据采集、特征提取和模型预测
 */
class TransportModeDetector(private val context: Context) {

    private val sensorCollector = SensorDataCollector(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 预测结果流
    private val _detectedMode = MutableStateFlow<TransportModePrediction?>(null)
    val detectedMode: StateFlow<TransportModePrediction?> = _detectedMode

    // 预测历史（用于平滑）
    private val predictionHistory = LinkedList<TransportModeLabel>()
    private val historySize = 3  // 保留最近 3 次预测

    // 是否正在检测
    private var isDetecting = false

    companion object {
        private const val TAG = "TransportModeDetector"
    }

    /**
     * 开始检测交通方式
     */
    fun startDetection() {
        if (isDetecting) {
            Log.w(TAG, "Already detecting")
            return
        }

        Log.d(TAG, "Starting transport mode detection")
        isDetecting = true

        // 开始采集传感器数据
        sensorCollector.startCollecting()

        // 监听数据窗口
        scope.launch {
            sensorCollector.windowFlow.collect { window ->
                window?.let {
                    processWindow(it)
                }
            }
        }
    }

    /**
     * 停止检测
     */
    fun stopDetection() {
        if (!isDetecting) return

        Log.d(TAG, "Stopping transport mode detection")
        isDetecting = false

        sensorCollector.stopCollecting()
        predictionHistory.clear()
    }

    /**
     * 更新 GPS 位置（从外部调用）
     */
    fun updateLocation(location: Location) {
        sensorCollector.updateGpsSpeed(location)
    }

    /**
     * 处理数据窗口
     */
    private suspend fun processWindow(window: SensorWindow) = withContext(Dispatchers.Default) {
        try {
            // 1. 提取特征
            val features = SensorFeatureExtractor.extractFeatures(window)
            Log.d(TAG, "Extracted features: accMean=${features.accXMean}, gpsSpeed=${features.gpsSpeedMean}")

            // 2. 运行模型预测
            val prediction = predictTransportMode(features)

            // 3. 平滑预测结果
            val smoothedPrediction = smoothPrediction(prediction)

            // 4. 更新检测结果
            _detectedMode.value = smoothedPrediction

            Log.d(TAG, "Detected mode: ${smoothedPrediction.mode} (confidence: ${smoothedPrediction.confidence})")

        } catch (e: Exception) {
            Log.e(TAG, "Error processing window", e)
        }
    }

    /**
     * 使用模型预测交通方式
     *
     * 使用简化的决策树分类器
     * TODO: 训练完 Random Forest 后，替换为真实的模型
     */
    private fun predictTransportMode(features: SensorFeatures): TransportModePrediction {
        // 转换特征为数组
        val featureArray = features.toFloatArray()

        // 使用简化的决策树分类器
        val (predictedClass, confidence) = SimpleDecisionTreeClassifier.predict(featureArray)

        // 转换类别索引为枚举
        val mode = when (predictedClass) {
            0 -> TransportModeLabel.WALKING
            1 -> TransportModeLabel.CYCLING
            2 -> TransportModeLabel.BUS
            3 -> TransportModeLabel.SUBWAY
            4 -> TransportModeLabel.DRIVING
            else -> TransportModeLabel.UNKNOWN
        }

        // 获取概率分布
        val probArray = SimpleDecisionTreeClassifier.predictProba(featureArray)
        val probabilities = mapOf(
            TransportModeLabel.WALKING to probArray[0],
            TransportModeLabel.CYCLING to probArray[1],
            TransportModeLabel.BUS to probArray[2],
            TransportModeLabel.SUBWAY to probArray[3],
            TransportModeLabel.DRIVING to probArray[4],
            TransportModeLabel.UNKNOWN to 0f
        )

        return TransportModePrediction(
            mode = mode,
            confidence = confidence,
            probabilities = probabilities
        )
    }

    /**
     * 平滑预测结果（使用多数投票）
     */
    private fun smoothPrediction(prediction: TransportModePrediction): TransportModePrediction {
        // 添加到历史
        predictionHistory.add(prediction.mode)
        if (predictionHistory.size > historySize) {
            predictionHistory.removeFirst()
        }

        // 如果历史不足，直接返回当前预测
        if (predictionHistory.size < historySize) {
            return prediction
        }

        // 多数投票
        val modeCount = predictionHistory.groupingBy { it }.eachCount()
        val majorityMode = modeCount.maxByOrNull { it.value }?.key ?: prediction.mode
        val majorityConfidence = modeCount[majorityMode]!!.toFloat() / historySize

        return TransportModePrediction(
            mode = majorityMode,
            confidence = majorityConfidence,
            probabilities = prediction.probabilities
        )
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        stopDetection()
        sensorCollector.cleanup()
        scope.cancel()
    }
}
