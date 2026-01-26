package com.example.EcoGo.service.churn;

import org.springframework.stereotype.Service;

/**
 * SupportService 实现类
 *
 * 当前版本：
 * - 不接 Mongo
 * - 不接模型
 * - 用于先跑通调用链
 */
@Service
public class SupportServiceImpl implements SupportService {

    @Override
    public ChurnRiskLevel getChurnRiskLevel(String userId) {
        // TODO:
        // 1. 从 Mongo 抽取特征
        // 2. 判断是否数据充足
        // 3. 调用内嵌模型推理
        // 4. 映射为 LOW / MEDIUM / HIGH

        return ChurnRiskLevel.INSUFFICIENT_DATA;
    }
}
