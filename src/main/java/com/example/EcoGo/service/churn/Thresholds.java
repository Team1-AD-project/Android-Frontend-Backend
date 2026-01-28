package com.example.EcoGo.service.churn;

import org.springframework.stereotype.Component;


@Component
public class Thresholds {


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
