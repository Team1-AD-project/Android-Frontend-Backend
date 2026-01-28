package com.example.EcoGo.service.churn;

public interface FeatureExtractor {

    /**
     * 根据 userId 抽取特征向量
     *
     * @param userId 用户唯一标识
     * @return 特征向量（允许为空或全 0，由上层判断是否充足）
     */
    ChurnFeatureVector extract(String userId);
}
