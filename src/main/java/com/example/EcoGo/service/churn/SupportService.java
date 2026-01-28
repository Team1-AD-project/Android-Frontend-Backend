package com.example.EcoGo.service.churn;

/**
 * SupportService 对外唯一暴露的接口
 */
public interface SupportService {

    /**

     * @param userId 用户唯一标识
     * @return 流失风险等级（LOW / MEDIUM / HIGH / INSUFFICIENT_DATA）
     */
    ChurnRiskLevel getChurnRiskLevel(String userId);
}
