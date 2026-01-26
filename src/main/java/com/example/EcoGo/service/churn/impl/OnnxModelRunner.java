package com.example.EcoGo.service.churn.impl;

import com.example.EcoGo.service.churn.ModelRunner;
import ai.onnxruntime.*;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Set;

/**
 * ONNX 推理实现：从 resources/ml/churn_model.onnx 加载
 * 输入：float[7]
 * 输出：概率 p in [0,1]
 */
@Component
public class OnnxModelRunner implements ModelRunner {

    private static final String MODEL_PATH = "/ml/churn_model.onnx";

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;
    private final String outputName;

    public OnnxModelRunner() {
        try {
            this.env = OrtEnvironment.getEnvironment();
            byte[] modelBytes = readAllBytesFromResource(MODEL_PATH);

            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            // opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT); // 可选

            this.session = env.createSession(modelBytes, opts);

            Set<String> inputs = session.getInputNames();
            Set<String> outputs = session.getOutputNames();
            this.inputName = inputs.iterator().next();
            this.outputName = outputs.iterator().next();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load ONNX model from " + MODEL_PATH, e);
        }
    }

    @Override
    public double predictProbability(float[] features) {
        if (features == null || features.length == 0) return Double.NaN;

        try {
            // 期望 shape: [1, N]
            long[] shape = new long[]{1, features.length};
            FloatBuffer fb = FloatBuffer.wrap(features);

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, fb, shape);
                 OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {

                Object out = result.get(outputName).get().getValue();

                // 常见情况：float[][] probs = [[p0, p1]]
                if (out instanceof float[][] probs && probs.length > 0) {
                    float[] row = probs[0];
                    if (row.length == 1) return clamp01(row[0]);
                    // 二分类，正类一般在 index=1
                    return clamp01(row[Math.min(1, row.length - 1)]);
                }

                // 兼容：float[] probs = [p0, p1]
                if (out instanceof float[] arr) {
                    if (arr.length == 1) return clamp01(arr[0]);
                    return clamp01(arr[Math.min(1, arr.length - 1)]);
                }

                // 兼容：Map 输出（如果导出时 zipmap=True）
                if (out instanceof Map<?, ?> map) {
                    // 取最大 key 的概率不可靠，这里尽量取 value 中的数值
                    for (Object v : map.values()) {
                        if (v instanceof Number n) return clamp01(n.doubleValue());
                    }
                }

                // 兜底：无法解析输出
                return Double.NaN;
            }
        } catch (Exception e) {
            // 推理失败视作数据不足（你要求不乱造）
            return Double.NaN;
        }
    }

    private static double clamp01(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return x;
    }

    private static byte[] readAllBytesFromResource(String classpath) throws Exception {
        try (InputStream is = OnnxModelRunner.class.getResourceAsStream(classpath)) {
            if (is == null) throw new IllegalStateException("Resource not found: " + classpath);
            return is.readAllBytes();
        }
    }
}
