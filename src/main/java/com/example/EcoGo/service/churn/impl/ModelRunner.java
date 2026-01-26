package com.example.EcoGo.service.churn;


public interface ModelRunner {

    /**
     * @param features 特征向量
     * @return 流失概率 [0, 1]
     */
    double predictProbability(float[] features);
}
