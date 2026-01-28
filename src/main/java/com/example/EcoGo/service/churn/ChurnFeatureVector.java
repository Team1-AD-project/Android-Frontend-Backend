package com.example.EcoGo.service.churn;

import java.util.Arrays;


public class ChurnFeatureVector {

    private final float[] features;

    public ChurnFeatureVector(float[] features) {
        this.features = (features == null) ? new float[0] : features;
    }

    public float[] getFeatures() {
        return features;
    }

  
    public float[] toArray() {
        return features;
    }


    public boolean isInsufficient() {
        if (features.length == 0) return true;

        int nonZeroCount = 0;
        for (float v : features) {
            if (v != 0f) {
                nonZeroCount++;
            }
        }
        return nonZeroCount < 2;
    }


    @Override
    public String toString() {
        return "ChurnFeatureVector" + Arrays.toString(features);
    }
}
