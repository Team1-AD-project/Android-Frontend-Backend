# EcoGo Android 游戏化闭环流程图

## 完整游戏化系统架构

```mermaid
graph TB
    Start([用户打开应用])
    Home[HomeFragment<br/>首页]
    
    %% 行程闭环
    RoutePlanner[RoutePlannerFragment<br/>路线规划]
    LocationSearch[LocationSearchFragment<br/>位置搜索]
    TripStart[TripStartFragment<br/>行程确认]
    TripProgress[TripInProgressFragment<br/>进行中-游戏化]
    TripSummary[TripSummaryFragment<br/>结算-奖励仪式]
    
    %% 活动闭环
    Activities[ActivitiesFragment<br/>活动列表]
    ActivityDetail[ActivityDetailFragment<br/>活动详情]
    
    %% 挑战闭环
    Challenges[ChallengesFragment<br/>挑战列表]
    ChallengeDetail[ChallengeDetailFragment<br/>挑战详情]
    
    %% 券包闭环
    Vouchers[VoucherFragment<br/>兑换券]
    VoucherDetail[VoucherDetailFragment<br/>券详情]
    
    %% 商店闭环
    Shop[ShopFragment<br/>商店]
    ItemDetail[ItemDetailFragment<br/>商品详情]
    
    %% 分享和社区
    Share[ShareImpactFragment<br/>分享成就]
    Feed[CommunityFeedFragment<br/>社区动态]
    Community[CommunityFragment<br/>社区]
    Profile[ProfileFragment<br/>个人]
    
    %% 地图
    MapGreenGo[MapGreenGoFragment<br/>地图+绿色点位]
    
    Start --> Home
    
    %% 行程闭环流程
    Home -->|推荐输入| LocationSearch
    LocationSearch --> RoutePlanner
    RoutePlanner -->|选择起点/终点| LocationSearch
    RoutePlanner -->|开始导航| TripStart
    TripStart -->|开始行程| TripProgress
    TripProgress -->|实时反馈<br/>里程碑| TripProgress
    TripProgress -->|结束行程| TripSummary
    
    %% 从结算页的多个出口
    TripSummary -->|查看排行| Community
    TripSummary -->|兑换奖励| Vouchers
    TripSummary -->|再来一次| RoutePlanner
    TripSummary -->|分享| Share
    
    %% 活动闭环
    Home -->|推荐活动| ActivityDetail
    Activities --> ActivityDetail
    ActivityDetail -->|开始路线| RoutePlanner
    ActivityDetail -->|参加活动| ActivityDetail
    ActivityDetail -->|签到| TripSummary
    
    %% 挑战闭环
    Community -->|挑战Tab| Challenges
    Challenges --> ChallengeDetail
    ChallengeDetail -->|接受挑战| ChallengeDetail
    ChallengeDetail -->|完成| TripSummary
    
    %% 券包闭环
    Profile -->|Redeem| Vouchers
    Vouchers --> VoucherDetail
    VoucherDetail -->|兑换| VoucherDetail
    
    %% 商店闭环
    Profile -->|长按Closet| Shop
    Shop --> ItemDetail
    ItemDetail -->|购买/装备| Profile
    
    %% 地图探索
    MapGreenGo -->|点击点位| MapGreenGo
    MapGreenGo -->|导航前往| RoutePlanner
    MapGreenGo -->|领取奖励| Profile
    
    %% 社区动态
    Community -->|动态Tab| Feed
    Feed -->|点击动态| ActivityDetail
    Feed --> ChallengeDetail
    Feed --> TripSummary
    
    %% 分享流程
    Share -->|分享| Share
    Profile -->|分享成就| Share
    
    style TripProgress fill:#90EE90
    style TripSummary fill:#FFD700
    style ChallengeDetail fill:#87CEEB
    style Share fill:#FFA07A
```

---

## 核心闭环详解

### 1. 行程闭环（主线）

```mermaid
sequenceDiagram
    participant User as 用户
    participant RP as RoutePlanner
    participant TS as TripStart
    participant TP as TripProgress
    participant TSM as TripSummary
    participant VM as NavigationViewModel
    
    User->>RP: 选择起点/终点
    RP->>RP: 计算路线选项
    User->>RP: 选择路线
    RP->>TS: navigate(route)
    TS->>User: 显示预计信息
    User->>TS: 点击"开始行程"
    TS->>VM: startNavigation()
    TS->>TP: navigate()
    
    loop 行程进行中
        TP->>VM: 观察realTimeCarbonSaved
        VM->>TP: 更新积分
        TP->>User: 动画反馈
        alt 达到里程碑
            TP->>User: 弹窗庆祝
        end
        alt 偏离路线
            TP->>User: 困惑提醒
        end
    end
    
    User->>TP: 点击"结束行程"
    TP->>VM: endNavigation()
    VM->>TP: completedTrip
    TP->>TSM: navigate()
    
    TSM->>User: 收据式展示
    TSM->>User: 小狮子庆祝
    TSM->>User: 积分动画
    
    alt 解锁成就
        TSM->>User: AchievementUnlockDialog
    end
    
    User->>TSM: 选择下一步
    alt 分享
        TSM->>Share: navigate()
    end
    alt 再来一次
        TSM->>RP: navigate()
    end
```

---

### 2. 挑战系统流程

```mermaid
stateDiagram-v2
    [*] --> ChallengeList: 查看挑战
    ChallengeList --> ChallengeDetail: 点击挑战
    ChallengeDetail --> Accepted: 接受挑战
    
    Accepted --> InProgress: 开始任务
    InProgress --> CompleteTrip: 完成一次行程
    CompleteTrip --> UpdateProgress: 进度+1
    UpdateProgress --> InProgress: 继续
    
    UpdateProgress --> Completed: 达到目标
    Completed --> UnlockAchievement: 解锁成就
    UnlockAchievement --> RewardPoints: 获得积分
    RewardPoints --> Share: 分享成就
    RewardPoints --> [*]
    
    note right of InProgress
        每次完成行程
        自动更新挑战进度
    end note
    
    note right of Completed
        小狮子庆祝动画
        成就解锁弹窗
        排行榜更新
    end note
```

---

### 3. 多闭环交互图

```mermaid
graph LR
    subgraph Trip[行程闭环]
        T1[规划] --> T2[开始]
        T2 --> T3[进行中]
        T3 --> T4[结算]
    end
    
    subgraph Activity[活动闭环]
        A1[列表] --> A2[详情]
        A2 --> A3[参与]
        A3 --> A4[签到]
    end
    
    subgraph Challenge[挑战闭环]
        C1[列表] --> C2[详情]
        C2 --> C3[接受]
        C3 --> C4[完成]
    end
    
    subgraph Reward[奖励系统]
        R1[积分]
        R2[成就]
        R3[徽章]
    end
    
    subgraph Social[社交系统]
        S1[分享]
        S2[动态]
        S3[排行榜]
    end
    
    %% 交互关系
    T4 --> R1
    T4 --> R2
    A4 --> R1
    C4 --> R1
    C4 --> R2
    C4 --> R3
    
    R2 --> S1
    T4 --> S1
    C4 --> S1
    
    S1 --> S2
    C4 --> S3
    T4 --> S3
```

---

## 游戏化设计原则应用

### 即时反馈
- ✅ TripInProgress - 实时积分累积
- ✅ 里程碑弹窗 - 每1km庆祝
- ✅ 小狮子动态表情 - 根据状态变化

### 奖励仪式感
- ✅ TripSummary - 收据式展示
- ✅ ValueAnimator - 积分增长动画
- ✅ AchievementUnlock - 成就解锁弹窗
- ✅ 环保等级 - A+/A/B评分

### 循环引导
- ✅ 每个终点都是下一个起点
- ✅ TripSummary提供4个下一步选择
- ✅ 挑战系统推动持续参与

### 社交分享
- ✅ 生成精美分享卡片
- ✅ 一键分享到社交平台
- ✅ 社区动态信息流

---

## 闭环完成度评分

| 闭环 | 完成度 | 核心功能 |
|------|--------|----------|
| 行程闭环 | ⭐⭐⭐⭐⭐ 100% | 规划→执行→奖励→分享 |
| 活动闭环 | ⭐⭐⭐⭐⭐ 100% | 浏览→参与→签到→奖励 |
| 挑战闭环 | ⭐⭐⭐⭐⭐ 100% | 接受→完成→解锁→分享 |
| 券包闭环 | ⭐⭐⭐⭐⭐ 100% | 兑换→使用→完成 |
| 商店闭环 | ⭐⭐⭐⭐☆ 95% | 浏览→试穿→购买→装备 |
| 地图闭环 | ⭐⭐⭐⭐⭐ 100% | 发现→导航→领取→分享 |

**总体完成度：99%**

---

**更新日期**：2026-02-02  
**版本**：2.0.0  
**状态**：✅ 所有功能已实施
