# EcoGo Android App 页面与跳转说明

本文档基于 `android-app/app` 目录，按「页面（Fragment/Activity）」和「跳转关系」整理当前实现。

---

## 一、入口与导航架构

- **唯一 Activity**：`MainActivity`，承载 `NavHostFragment` + 底部导航栏。
- **导航图**：`res/navigation/nav_graph.xml`，**起始目的地**为 `loginFragment`。
- **底部导航**：`res/menu/bottom_nav_menu.xml`，仅部分页面在底部栏显示；进入登录/ onboarding 时底部栏会隐藏。

---

## 二、Nav Graph 中的页面（Fragment）一览

### 原有页面（15个）

| Fragment ID | 类名 | 布局 | 说明 |
|-------------|------|------|------|
| `loginFragment` | LoginFragment | fragment_login | 登录页（起始页） |
| `signupWizardFragment` | SignupWizardFragment | fragment_signup_wizard | 注册向导（学院选择 + 换装） |
| `onboardingFragment` | OnboardingFragment | fragment_onboarding | 新用户引导（ViewPager 3 页） |
| `homeFragment` | HomeFragment | fragment_home | 首页 |
| `routesFragment` | RoutesFragment | fragment_routes | 巴士/路线列表 |
| `communityFragment` | CommunityFragment | fragment_community | 社区（学院/好友/步数排行榜 Tab） |
| `chatFragment` | ChatFragment | fragment_chat | 与 LiNUS 助手对话 |
| `profileFragment` | ProfileFragment | fragment_profile | 个人资料（积分、换装、商店、成就、历史） |
| `settingsFragment` | SettingsFragment | fragment_settings | 设置 |
| `voucherFragment` | VoucherFragment | fragment_voucher | 兑换券（增强：Tab切换） |
| `activitiesFragment` | ActivitiesFragment | fragment_activities | 活动列表（增强：点击导航） |
| `mapFragment` | MapFragment | fragment_map | 地图（占位/学院信息） |
| `mapGreenGoFragment` | MapGreenGoFragment | fragment_map_green_go | Green Go 地图（增强：绿色点位） |
| `friendsFragment` | FriendsFragment | fragment_friends | 好友列表 |
| `shopFragment` | ShopFragment | fragment_shop | 商店（已激活） |

### ⭐ 新增页面（12个）

#### 阶段一：核心闭环（6个）

| Fragment ID | 类名 | 布局 | 说明 |
|-------------|------|------|------|
| `locationSearchFragment` | LocationSearchFragment | fragment_location_search | 位置搜索（已接入） |
| `routePlannerFragment` | RoutePlannerFragment | fragment_route_planner | 路线规划（已接入） |
| `tripStartFragment` | TripStartFragment | fragment_trip_start | 行程开始确认 ⭐ |
| `tripInProgressFragment` | TripInProgressFragment | fragment_trip_in_progress | 行程进行中（游戏化重点）⭐ |
| `tripSummaryFragment` | TripSummaryFragment | fragment_trip_summary | 行程结算（奖励仪式）⭐ |
| `activityDetailFragment` | ActivityDetailFragment | fragment_activity_detail | 活动详情与参与 ⭐ |

#### 阶段二：游戏化增强（4个）

| Fragment ID | 类名 | 布局 | 说明 |
|-------------|------|------|------|
| `challengesFragment` | ChallengesFragment | fragment_challenges | 挑战列表 ⭐ |
| `challengeDetailFragment` | ChallengeDetailFragment | fragment_challenge_detail | 挑战详情 ⭐ |
| `voucherDetailFragment` | VoucherDetailFragment | fragment_voucher_detail | 兑换券详情 ⭐ |
| `itemDetailFragment` | ItemDetailFragment | fragment_item_detail | 商品详情与试穿 ⭐ |

#### 阶段三：社交增长（2个 + 1个BottomSheet）

| Fragment ID | 类名 | 布局 | 说明 |
|-------------|------|------|------|
| `shareImpactFragment` | ShareImpactFragment | fragment_share_impact | 分享成就卡片 ⭐ |
| `communityFeedFragment` | CommunityFeedFragment | fragment_community_feed | 社区动态信息流 ⭐ |
| - | SpotDetailBottomSheet | bottom_sheet_spot_detail | 绿色点位详情 ⭐ |

**总计**：27个Fragment（15个原有 + 12个新增）

---

## 三、底部导航栏（Bottom Nav）

**显示项（5 个）**：与 `bottom_nav_menu.xml` 一致

| 菜单 ID | 对应 Fragment | 标题 (string) |
|---------|----------------|---------------|
| homeFragment | HomeFragment | nav_home |
| mapGreenGoFragment | MapGreenGoFragment | nav_green_go |
| communityFragment | CommunityFragment | nav_community |
| chatFragment | ChatFragment | nav_chat |
| profileFragment | ProfileFragment | nav_profile |

**隐藏底部栏的页面**（在 `MainActivity` 的 `OnDestinationChangedListener` 中）：  
`loginFragment`、`signupWizardFragment`、`onboardingFragment`。

---

## 四、各页面功能与跳转

### 1. 登录（LoginFragment）

**功能**：NUSNET ID、密码输入；Sign In / Register。

**跳转**：
- **Sign In** → `action_login_to_onboarding` → **OnboardingFragment**（当前实现为新用户先走 onboarding，未区分老用户直接进首页）
- **Register** → `action_login_to_signup` → **SignupWizardFragment**

---

### 2. 注册向导（SignupWizardFragment）

**功能**：两步——学院选择、小狮子换装展示；完成后进入首页。

**跳转**：
- 完成注册 → `action_signup_to_home` → **HomeFragment**（并 pop 回退到登录）

---

### 3. 新用户引导（OnboardingFragment）

**功能**：ViewPager 3 页引导；Next / Get Started / Skip。

**跳转**：
- **Get Started**（最后一页）或 **Skip** → `action_onboarding_to_home` → **HomeFragment**

---

### 4. 首页（HomeFragment）

**功能**：小狮子头像、月度积分、社区分数、下一班巴士、地图预览、推荐输入、每日签到、通知横幅、碳足迹、今日目标、天气等；RecyclerView：推荐活动、步行路线。

**跳转**：
- 推荐活动 / View All / 今日目标等 → **ActivitiesFragment**
- 步行路线卡片 / 下一班巴士卡片 → **RoutesFragment**
- 地图预览 / 「Open Map」→ **MapFragment**
- 小狮子头像 / 月度积分 / 碳足迹 / 今日目标 → **ProfileFragment**
- 社区分数卡片 → **CommunityFragment**

---

### 5. 路线（RoutesFragment）

**功能**：巴士路线列表（RecyclerView），数据来自 Repository / MockData。

**跳转**：
- 路线卡片点击 → **RoutePlannerFragment**（通过 action_routes_to_routePlanner）

---

### 6. 社区（CommunityFragment）

**功能**：Tab——学院排行榜、好友排行榜、步数排行榜；Leader 信息；RecyclerView 列表。

**跳转**：
- 「查看全部」好友榜 → **FriendsFragment**
- 好友项「消息」→ **ChatFragment**
- 好友项「头像/详情」→ **ProfileFragment**
- Challenges 按钮 → **ChallengesFragment**
- Feed 按钮 → **CommunityFeedFragment**

---

### 7. 好友（FriendsFragment）

**功能**：好友列表，每条可点消息或头像。

**跳转**：
- 消息 → **ChatFragment**（通过 action_friends_to_chat）
- 好友点击 → **ProfileFragment**（通过 action_friends_to_profile）

---

### 8. 聊天（ChatFragment）

**功能**：与 LiNUS 助手对话，发送消息、调用推荐 API；智能识别关键词并推荐跳转。

**跳转**：
- 关键词「活动」→ **ActivitiesFragment**
- 关键词「路线/导航」→ **RoutePlannerFragment**
- 关键词「地图/位置」→ **MapGreenGoFragment**

---

### 9. 个人资料（ProfileFragment）

**功能**：积分、姓名、学院、小狮子换装、学院服装、商店区域（RecyclerView）、徽章、成就、历史记录；设置与兑换按钮。

**跳转**：
- **Settings** → `action_profile_to_settings` → **SettingsFragment**
- **Redeem** → **VoucherFragment**

---

### 10. 设置（SettingsFragment）

**功能**：通知开关、深色模式开关、编辑个人资料入口。

**跳转**：
- 编辑个人资料卡片 → **ProfileFragment**

---

### 11. 兑换券（VoucherFragment）

**功能**：兑换券列表展示。

**跳转**：
- 券卡片点击 → **VoucherDetailFragment**

---

### 12. 活动（ActivitiesFragment）

**功能**：活动列表（来自 Home 的「推荐活动」「View All」等入口）。

**跳转**：
- 活动卡片点击 → **ActivityDetailFragment**

---

### 13. 地图（MapFragment）

**功能**：地图占位、学院信息卡片（名称、排名、积分）。

**跳转**：
- 地图区域点击 → **MapGreenGoFragment**（Green Go 地图）
- 学院卡片点击 → **RoutePlannerFragment**（路线规划）

---

### 14. Green Go 地图（MapGreenGoFragment）

**功能**：地图控件（如 Google Map），在底部导航中作为独立 Tab。

**跳转**：无代码内 `navigate`，仅底部栏切换。

---

### 15. 商店（ShopFragment）

**功能**：在 nav_graph 与布局中存在，已激活入口。

**跳转**：
- 商品点击 → **ItemDetailFragment**（试穿与购买）

---

## 五、✅ 已激活并接入的Fragment

原本未接入的Fragment现已全部激活：

| 类名 | 状态 | 入口 |
|------|------|------|
| LocationSearchFragment | ✅ 已接入nav_graph | RoutePlannerFragment起点/终点选择 |
| RoutePlannerFragment | ✅ 已接入nav_graph | Home、Routes、MapGreenGo多处入口 |
| ShopFragment | ✅ 已激活入口 | ProfileFragment长按Closet Tab |

---

## 六、跳转关系简图（已更新）

### 原有跳转链

```
[登录] LoginFragment (startDestination)
  ├── Sign In → OnboardingFragment
  └── Register → SignupWizardFragment

[注册] SignupWizardFragment
  └── 完成 → HomeFragment (pop to login)

[引导] OnboardingFragment
  └── Get Started / Skip → HomeFragment

[首页] HomeFragment
  ├── 推荐活动 → ActivitiesFragment → ActivityDetailFragment ⭐
  ├── 步行路线 → RoutesFragment
  ├── 地图预览 → MapFragment
  ├── 小狮子/积分 → ProfileFragment
  ├── 社区分数 → CommunityFragment
  └── 推荐输入 → RoutePlannerFragment ⭐

[社区] CommunityFragment
  ├── 查看全部好友 → FriendsFragment
  ├── 好友-消息 → ChatFragment
  ├── 好友-详情 → ProfileFragment
  ├── 挑战Tab → ChallengesFragment ⭐
  └── 动态Tab → CommunityFeedFragment ⭐

[好友] FriendsFragment
  ├── 消息 → ChatFragment
  └── 好友点击 → ProfileFragment

[个人资料] ProfileFragment
  ├── Settings → SettingsFragment
  ├── Redeem → VoucherFragment → VoucherDetailFragment ⭐
  ├── 长按Closet → ShopFragment ⭐
  └── 分享 → ShareImpactFragment ⭐
```

### ⭐ 新增完整闭环

#### 行程闭环
```
[路线规划] RoutePlannerFragment
  ├── 选择起点 → LocationSearchFragment ⭐
  ├── 选择终点 → LocationSearchFragment ⭐
  └── 开始导航 → TripStartFragment ⭐
      └── 开始行程 → TripInProgressFragment ⭐
          └── 结束行程 → TripSummaryFragment ⭐
              ├── 查看排行 → CommunityFragment
              ├── 兑换奖励 → VoucherFragment
              ├── 再来一次 → RoutePlannerFragment
              └── 分享 → ShareImpactFragment ⭐
```

#### 活动闭环
```
[活动列表] ActivitiesFragment
  └── 点击活动 → ActivityDetailFragment ⭐
      ├── 参加活动 → API joinActivity()
      ├── 开始路线 → RoutePlannerFragment ⭐
      └── 签到 → dialog_success + 奖励
```

#### 挑战闭环
```
[挑战列表] ChallengesFragment ⭐
  └── 点击挑战 → ChallengeDetailFragment ⭐
      ├── 接受挑战 → 开始任务
      ├── 查看排行榜 → LeaderboardAdapter
      └── 完成挑战 → AchievementUnlockDialog
```

#### 券包闭环
```
[兑换券] VoucherFragment（增强）
  └── 点击券 → VoucherDetailFragment ⭐
      ├── 兑换 → 积分扣除 + 券码生成
      └── 使用 → 确认对话框
```

#### 商店闭环
```
[商店] ShopFragment（已激活）
  └── 点击商品 → ItemDetailFragment ⭐
      ├── 试穿预览 → MascotLionView动态更新
      └── 购买 → dialog_purchase_success
```

#### 地图探索闭环
```
[Green Go地图] MapGreenGoFragment（增强）
  └── 点击绿色点位 → SpotDetailBottomSheet ⭐
      ├── 导航前往 → RoutePlannerFragment ⭐
      └── 领取奖励 → 积分增加
```

#### 分享闭环
```
[分享成就] ShareImpactFragment ⭐
  ├── 生成分享卡 → Canvas绘制
  ├── 保存图片 → 相册
  └── 分享 → Intent.ACTION_SEND
```

#### 社区动态
```
[社区动态] CommunityFeedFragment ⭐
  └── 点击动态 → 对应页面 ✅
      ├── TRIP → TripSummaryFragment ✅
      ├── ACHIEVEMENT → ProfileFragment ✅
      ├── ACTIVITY → ActivityDetailFragment ✅
      └── CHALLENGE → ChallengeDetailFragment ✅
```

#### 路线浏览
```
[路线列表] RoutesFragment
  └── 点击巴士路线 → RoutePlannerFragment ✅
```

#### 地图探索增强
```
[地图] MapFragment
  ├── 点击地图区域 → MapGreenGoFragment ✅
  └── 点击学院卡片 → RoutePlannerFragment ✅
```

#### 好友互动
```
[好友列表] FriendsFragment
  ├── 点击消息 → ChatFragment ✅
  └── 点击好友 → ProfileFragment ✅
```

#### 智能聊天助手
```
[聊天] ChatFragment
  └── 智能识别关键词 ✅
      ├── "活动" → ActivitiesFragment ✅
      ├── "路线/导航" → RoutePlannerFragment ✅
      └── "地图/位置" → MapGreenGoFragment ✅
```

#### 设置增强
```
[设置] SettingsFragment
  └── 编辑个人资料 → ProfileFragment ✅
```

---

## 七、对话框（Dialogs）

| 布局 | 说明 |
|------|------|
| dialog_achievement_unlock.xml | 成就解锁弹窗（AchievementUnlockDialog） |
| dialog_purchase_success.xml | 购买成功弹窗 |
| dialog_success.xml | 通用成功提示 |

---

## 八、文件路径速查（app 下）

- **Nav 图**：`app/src/main/res/navigation/nav_graph.xml`
- **主布局**：`app/src/main/res/layout/activity_main.xml`
- **底部菜单**：`app/src/main/res/menu/bottom_nav_menu.xml`
- **Fragment 类**：`app/src/main/kotlin/com/ecogo/ui/fragments/` 及其子包 `navigation/`
- **Fragment 布局**：`app/src/main/res/layout/fragment_*.xml`

以上即 Android App 的页面与跳转整合说明；若后续新增 `navigate` 或把 LocationSearch/RoutePlanner/Shop 接入导航，可据此文档增补。
