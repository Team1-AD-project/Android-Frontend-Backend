package com.ecogo.app.ml

/**
 * 传感器原始数据
 */
data class SensorRawData(
    val timestamp: Long,
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val gpsSpeed: Float,  // 单位：m/s
    val pressure: Float   // 气压，单位：hPa
)

/**
 * 传感器数据窗口（5秒窗口）
 */
data class SensorWindow(
    val startTime: Long,
    val endTime: Long,
    val data: List<SensorRawData>,
    val label: TransportModeLabel? = null  // 用于训练时的标签
)

/**
 * 交通方式标签（用于数据标注）
 */
enum class TransportModeLabel {
    WALKING,
    CYCLING,
    BUS,
    SUBWAY,
    DRIVING,
    UNKNOWN
}

/**
 * 提取的特征
 */
data class SensorFeatures(
    // 加速度计统计特征 (3轴 × 7特征 = 21)
    val accXMean: Float,
    val accXStd: Float,
    val accXMax: Float,
    val accXMin: Float,
    val accXRange: Float,
    val accXMedian: Float,
    val accXSma: Float,  // Signal Magnitude Area

    val accYMean: Float,
    val accYStd: Float,
    val accYMax: Float,
    val accYMin: Float,
    val accYRange: Float,
    val accYMedian: Float,
    val accYSma: Float,

    val accZMean: Float,
    val accZStd: Float,
    val accZMax: Float,
    val accZMin: Float,
    val accZRange: Float,
    val accZMedian: Float,
    val accZSma: Float,

    // 陀螺仪统计特征 (3轴 × 7特征 = 21)
    val gyroXMean: Float,
    val gyroXStd: Float,
    val gyroXMax: Float,
    val gyroXMin: Float,
    val gyroXRange: Float,
    val gyroXMedian: Float,
    val gyroXSma: Float,

    val gyroYMean: Float,
    val gyroYStd: Float,
    val gyroYMax: Float,
    val gyroYMin: Float,
    val gyroYRange: Float,
    val gyroYMedian: Float,
    val gyroYSma: Float,

    val gyroZMean: Float,
    val gyroZStd: Float,
    val gyroZMax: Float,
    val gyroZMin: Float,
    val gyroZRange: Float,
    val gyroZMedian: Float,
    val gyroZSma: Float,

    // 组合特征
    val accMagnitudeMean: Float,      // sqrt(x² + y² + z²) 的均值
    val accMagnitudeStd: Float,
    val accMagnitudeMax: Float,

    val gyroMagnitudeMean: Float,
    val gyroMagnitudeStd: Float,
    val gyroMagnitudeMax: Float,

    // GPS特征
    val gpsSpeedMean: Float,
    val gpsSpeedStd: Float,
    val gpsSpeedMax: Float,

    // 气压特征
    val pressureMean: Float,
    val pressureStd: Float,

    // 频域特征（可选，暂时不实现）
    // val dominantFrequency: Float,
    // val spectralEntropy: Float
) {
    /**
     * 转换为浮点数组（用于模型输入）
     */
    fun toFloatArray(): FloatArray {
        return floatArrayOf(
            accXMean, accXStd, accXMax, accXMin, accXRange, accXMedian, accXSma,
            accYMean, accYStd, accYMax, accYMin, accYRange, accYMedian, accYSma,
            accZMean, accZStd, accZMax, accZMin, accZRange, accZMedian, accZSma,
            gyroXMean, gyroXStd, gyroXMax, gyroXMin, gyroXRange, gyroXMedian, gyroXSma,
            gyroYMean, gyroYStd, gyroYMax, gyroYMin, gyroYRange, gyroYMedian, gyroYSma,
            gyroZMean, gyroZStd, gyroZMax, gyroZMin, gyroZRange, gyroZMedian, gyroZSma,
            accMagnitudeMean, accMagnitudeStd, accMagnitudeMax,
            gyroMagnitudeMean, gyroMagnitudeStd, gyroMagnitudeMax,
            gpsSpeedMean, gpsSpeedStd, gpsSpeedMax,
            pressureMean, pressureStd
        )
    }

    companion object {
        const val FEATURE_SIZE = 53  // 总特征数
    }
}

/**
 * 预测结果
 */
data class TransportModePrediction(
    val mode: TransportModeLabel,
    val confidence: Float,  // 置信度 0-1
    val probabilities: Map<TransportModeLabel, Float>  // 每个类别的概率
)
