package com.example.EcoGo.service.churn;

import org.springframework.stereotype.Component;

/**
 * 概率 -> 风险等级映射
 * 先用固定阈值，后续可改为从 resources/ml/thresholds.json 或 application.yml 读取。
 */
@Component
public class Thresholds {

    // 可按需调整
    private final double lowUpper = 0.33;
    private final double mediumUpper = 0.66;

    public ChurnRiskLevel toLevel(double p) {
        if (Double.isNaN(p)) {
            return ChurnRiskLevel.INSUFFICIENT_DATA;
        }
        if (p < lowUpper) return ChurnRiskLevel.LOW;
        if (p < mediumUpper) return ChurnRiskLevel.MEDIUM;
        return ChurnRiskLevel.HIGH;
    }
}
