package com.example.EcoGo.service.churn.impl;

import com.example.EcoGo.service.churn.ModelRunner;
import ai.onnxruntime.*;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Set;

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
            this.session = env.createSession(modelBytes, opts);

            Set<String> inputs = session.getInputNames();
            Set<String> outputs = session.getOutputNames();

            this.inputName = inputs.iterator().next();
            this.outputName = pickProbabilityOutputName(outputs);

            System.out.println(">>> OnnxModelRunner loaded, input=" + inputName + ", output=" + outputName + ", allOutputs=" + outputs);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load ONNX model from " + MODEL_PATH, e);
        }
    }

    private static String pickProbabilityOutputName(Set<String> outputs) {
        for (String o : outputs) {
            String s = o.toLowerCase();
            if (s.contains("prob") || s.contains("proba") || s.contains("probabilities")) return o;
        }
        String last = null;
        for (String o : outputs) last = o;
        return last != null ? last : outputs.iterator().next();
    }

    @Override
    public double predictProbability(float[] features) {
        if (features == null || features.length == 0) return Double.NaN;

        try {
            long[] shape = new long[]{1, features.length};
            FloatBuffer fb = FloatBuffer.wrap(features);

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, fb, shape);
                 OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {

                double p = tryParseProbability(result.get(outputName).get().getValue());
                if (!Double.isNaN(p)) return p;

                for (Map.Entry<String, OnnxValue> e : result) {
                    double p2 = tryParseProbability(e.getValue().getValue());
                    if (!Double.isNaN(p2)) {
                        System.out.println(">>> OnnxModelRunner fallback output used: " + e.getKey());
                        return p2;
                    }
                }

                return Double.NaN;
            }
        } catch (Exception e) {
            System.out.println(">>> OnnxModelRunner exception: " + e.getMessage());
            return Double.NaN;
        }
    }

    private static double tryParseProbability(Object out) {
        if (out == null) return Double.NaN;

        // float[][] probs = [[p0, p1]] / [[p]]
        if (out instanceof float[][] probs && probs.length > 0) {
            float[] row = probs[0];
            if (row.length == 1) return clamp01(row[0]);
            return clamp01(row[Math.min(1, row.length - 1)]);
        }

        // float[] probs = [p0, p1] / [p]
        if (out instanceof float[] arr) {
            if (arr.length == 1) return clamp01(arr[0]);
            return clamp01(arr[Math.min(1, arr.length - 1)]);
        }

        // double[][] / double[]
        if (out instanceof double[][] dprobs && dprobs.length > 0) {
            double[] row = dprobs[0];
            if (row.length == 1) return clamp01(row[0]);
            return clamp01(row[Math.min(1, row.length - 1)]);
        }
        if (out instanceof double[] darr) {
            if (darr.length == 1) return clamp01(darr[0]);
            return clamp01(darr[Math.min(1, darr.length - 1)]);
        }

        // Map 输出（zipmap=True）
        if (out instanceof Map<?, ?> map) {
            for (Object v : map.values()) {
                if (v instanceof Number n) return clamp01(n.doubleValue());
            }
        }

        return Double.NaN;
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
