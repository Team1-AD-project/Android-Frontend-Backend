package com.example.EcoGo.service.churn;

import java.util.Arrays;

/**
 * 用户流失预测所需的特征向量
 * - 只负责保存 float[] 与“数据是否充足”的判定
 * - 特征顺序由 FeatureExtractor 保证（必须与 ONNX 训练一致）
 */
public class ChurnFeatureVector {

    private final float[] features;

    public ChurnFeatureVector(float[] features) {
        this.features = (features == null) ? new float[0] : features;
    }

    public float[] getFeatures() {
        return features;
    }

    /** ONNX 输入向量 */
    public float[] toArray() {
        return features;
    }

    /**
     * 数据是否不足：
     * 你要求：关键字段全 0 / 缺失 => INSUFFICIENT_DATA（不随机造）
     */
    public boolean isInsufficient() {
        if (features.length == 0) return true;
        for (float v : features) {
            if (v != 0f) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChurnFeatureVector" + Arrays.toString(features);
    }
}
