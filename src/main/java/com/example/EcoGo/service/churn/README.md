# EcoGo – User Churn Risk Support Service

## 1. 模块简介

**User Churn Risk Support Service** 是 EcoGo 后端中的一个支持型服务模块（Support Service），  
用于对用户进行 **流失风险（Churn Risk）评估**。

该模块基于用户在 MongoDB 中的历史行为数据，通过**内嵌的机器学习模型（ONNX）**进行推理，  
返回统一、标准化的流失风险等级，供系统其他模块使用。

> 本模块不依赖外部 ML 服务，不需要单独部署，随 EcoGo 仓库一并提交与运行。

---

## 2. 功能概览

- 根据用户行为数据预测流失风险
- 返回标准化风险等级（LOW / MEDIUM / HIGH）
- 数据不足时返回明确状态（INSUFFICIENT_DATA）
- 支持后端内部调用与 HTTP API 调用
- 模型与业务逻辑解耦，便于后续迭代

---

## 3. 风险等级定义

| Risk Level | 含义 |
|-----------|------|
| `LOW` | 用户活跃度高，流失风险低 |
| `MEDIUM` | 用户活跃度中等，存在潜在流失风险 |
| `HIGH` | 用户活跃度明显下降，高流失风险 |
| `INSUFFICIENT_DATA` | 用户数据不足，无法进行可靠评估 |

---

## 4. 数据与特征说明

### 数据来源
- 数据库：MongoDB
- Collection：`users`

### 当前模型特征（v1）

特征定义由 `feature_schema.json` 描述：

```json
{
  "version": 1,
  "feature_dim": 7,
  "features": [
    "stats.totalTrips",
    "stats.activeDays",
    "stats.completedTasks",
    "totalCarbon",
    "totalPoints",
    "currentPoints",
    "vip.isActive|vip.level"
  ]
}
