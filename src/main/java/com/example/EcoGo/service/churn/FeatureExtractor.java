package com.example.EcoGo.service.churn;

/**
 * 特征抽取接口
 *
 * 职责：
 * - 从数据源（Mongo / 未来可扩展）中抽取用户特征
 *
 * 注意：
 * - 不做模型推理
 * - 不做风险等级判断
 */
public interface FeatureExtractor {

    /**
     * 根据 userId 抽取特征向量
     *
     * @param userId 用户唯一标识
     * @return 特征向量（允许为空或全 0，由上层判断是否充足）
     */
    ChurnFeatureVector extract(String userId);
}
