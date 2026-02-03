# EcoGo Admin 前端页面与跳转说明

本文档按「页面」和「跳转关系」整理当前前端已实现的功能。

---

## 一、路由与入口

- **单页入口**：`index.html`，通过 `#/path` 的 hash 路由切换页面。
- **路由入口**：`js/app.js`，根据 `window.location.hash` 解析路径并加载对应 Page 组件。
- **未匹配路由**：显示 `404 Not Found`。

---

## 二、页面列表与路由映射

| 路由 | 页面组件 | 说明 |
|------|----------|------|
| `#/`、`#/dashboard` | DashboardPage | 仪表盘概览 |
| `#/login` | LoginPage | 登录页（无侧边栏） |
| `#/users` | UserListPage | 用户管理（占位） |
| `#/activities` | ActivityListPage | 活动管理 |
| `#/leaderboard`、`#/leaderboard?period=xxx` | LeaderboardPage | 排行榜管理 |
| `#/carbon` | CarbonCreditsPage | 碳积分统计与分析 |
| `#/ads` | AdvertisementListPage | 广告列表 |
| `#/ad/new` | AdvertisementFormPage | 新建广告 |
| `#/ad/edit`、`#/ad/edit/:id` | AdvertisementFormPage | 编辑广告（id 从 hash 中解析） |
| `#/settings` | SettingsPage | 系统设置 |

---

## 三、各页面功能与跳转

### 1. 登录页 `#/login`（LoginPage）

**功能：**

- 品牌区展示（EcoGo Admin、slogan、特性列表）。
- 登录表单：邮箱、密码、记住我、忘记密码链接。
- 提交后：前端校验 → 写 `localStorage`（isLoggedIn、userEmail）→ 跳转 `#/dashboard`。
- 底部「Contact Administrator」链接指向 `#/register`（当前无注册页，会 404）。

**跳转：**

- 成功登录 → `#/dashboard`。

---

### 2. 仪表盘 `#/`、`#/dashboard`（DashboardPage）

**功能：**

- 使用 Layout（侧边栏 + 顶栏），拉取用户、广告、活动数据。
- 四个统计卡片：总用户数、广告数、活动数、碳积分总和。
- 最近活动表格（前 5 条），带「View All」→ `#/activities`。
- 用户碳积分 Top5 表格，带「View All」→ `#/users`。
- 广告概览表格（前 5 条），带「View All」→ `#/ads`。
- 加载失败时展示错误信息与重试按钮。

**跳转：**

- 最近活动「View All」→ `#/activities`。
- 用户排行「View All」→ `#/users`。
- 广告概览「View All」→ `#/ads`。
- 侧边栏/面包屑由 Layout 提供（见下文）。

---

### 3. 用户管理 `#/users`（UserListPage）

**功能：**

- 占位页：「Coming Soon」，说明用户管理开发中。
- 提供「Back to Dashboard」按钮。

**跳转：**

- 「Back to Dashboard」→ `#/dashboard`。

---

### 4. 活动管理 `#/activities`（ActivityListPage）

**功能：**

- 活动列表表格：活动名称、类型、状态、奖励积分、参与人数、开始时间、操作。
- 操作：草稿可「发布」、每行可「编辑」「删除」；顶部「+ 新增活动」。
- 发布、删除调用 API，成功后重新 render 列表。
- 编辑、新增目前为 `alert` 占位（开发中）。

**跳转：**

- 仅通过侧边栏/面包屑离开（无页面内按钮跳转其他业务页）。

---

### 5. 排行榜 `#/leaderboard`（LeaderboardPage）

**功能：**

- 周期选择：URL 支持 `?period=xxx`，页面内下拉框切换周期会更新 hash。
- 统计卡片：本周参与人数、榜上 VIP 数、平均步数、已发放奖励。
- 搜索：按昵称或用户 ID 过滤。
- 本周前三名展示（金银铜）。
- 完整排名表格：排名、用户、步数、进度条、周期；支持分页（每页 15 条）。
- 导出 CSV（当前筛选结果）。
- 使用 `leaderboardService.getPeriods()`、`getRankings(period)`。

**跳转：**

- 切换周期 → `#/leaderboard?period=xxx`（同页刷新数据）。

---

### 6. 碳积分 `#/carbon`（CarbonCreditsPage）

**功能：**

- 周期选择：7/30/90 天（仅前端展示，未与接口联动）。
- 统计卡片：总碳减排、30 天活跃用户、已兑换积分、进行中活动。
- 碳积分趋势图（柱状）、积分来源分布图。
- 碳活动热力图（12 周）、最近交易表格（示例数据）。
- 数据来自 `statisticsService`（getDashboardStats、getTotalCarbonReduction 等）。
- 失败时展示错误与重试。

**跳转：**

- 仅通过侧边栏/面包屑离开。

---

### 7. 广告列表 `#/ads`（AdvertisementListPage）

**功能：**

- 列表表格：广告名称、状态、开始/结束日期、操作（编辑、删除）。
- 顶部「+ New Advertisement」按钮。
- 删除：确认后调 API，成功后重新 init 列表。
- 数据来自 `advertisementService.getAdvertisements()`。

**跳转：**

- 「+ New Advertisement」→ `#/ad/new`。
- 每行「Edit」→ `#/ad/edit/:id`。

---

### 8. 新建/编辑广告 `#/ad/new`、`#/ad/edit`、`#/ad/edit/:id`（AdvertisementFormPage）

**功能：**

- 根据 hash 是否匹配 `#/ad/edit/(.+)` 判断是编辑还是新建；编辑时用 id 拉取详情。
- 表单：广告名称、状态（Active/Inactive/Paused）、开始日期、结束日期。
- 提交：调用 `advertisementService.saveAdvertisement(adData)`，成功后跳回 `#/ads`。
- 取消按钮 → `#/ads`。

**跳转：**

- 保存成功 → `#/ads`。
- 「Cancel」→ `#/ads`。

---

### 9. 设置 `#/settings`（SettingsPage）

**功能：**

- 通用设置：平台名称、联系邮箱、语言（English/Chinese）。
- 通知：邮件通知、周报、用户活动提醒（开关，仅 UI）。
- 安全：当前密码、新密码、确认密码、「Update Password」按钮（仅 UI）。
- 系统信息：版本、环境、数据库、端口（静态展示）。
- 当前无保存逻辑，仅为界面。

**跳转：**

- 仅通过侧边栏/面包屑离开。

---

## 四、公共布局与全局跳转（Layout）

所有非登录页共用 `Layout.js`：

- **侧边栏**  
  - Overview：Dashboard → `#/dashboard`  
  - Management：Advertisements → `#/ads`，Activities → `#/activities`，Leaderboard → `#/leaderboard`  
  - Users：User Management → `#/users`，Carbon Credits → `#/carbon`  
  - System：Settings → `#/settings`  

- **顶栏**  
  - 面包屑：Home → `#/dashboard`，当前页标题为文本（不跳转）。  
  - 搜索、通知为按钮（暂无逻辑），用户信息展示「Admin / Administrator」。

**说明：** 登录态未在路由层做拦截，任何 hash 均可直接访问；登录页仅通过表单提交后主动跳转到 `#/dashboard`。

---

## 五、跳转关系简图

```
#/login
    └── 登录成功 → #/dashboard

#/dashboard (#/)
    ├── View All（最近活动）→ #/activities
    ├── View All（用户排行）→ #/users
    └── View All（广告概览）→ #/ads

#/users
    └── Back to Dashboard → #/dashboard

#/activities
    └── （仅侧边栏/面包屑）

#/leaderboard、#/leaderboard?period=xxx
    └── 周期切换 → #/leaderboard?period=yyy

#/carbon
    └── （仅侧边栏/面包屑）

#/ads
    ├── + New Advertisement → #/ad/new
    └── Edit → #/ad/edit/:id

#/ad/new、#/ad/edit、#/ad/edit/:id
    ├── Save → #/ads
    └── Cancel → #/ads

#/settings
    └── （仅侧边栏/面包屑）

侧边栏（所有已登录布局页）
    ├── Dashboard → #/dashboard
    ├── Advertisements → #/ads
    ├── Activities → #/activities
    ├── Leaderboard → #/leaderboard
    ├── User Management → #/users
    ├── Carbon Credits → #/carbon
    └── Settings → #/settings
```

---

## 六、服务依赖（按页面）

| 页面 | 使用的 Service |
|------|-----------------|
| Dashboard | userService, advertisementService, activityService |
| UserList | 无（占位） |
| ActivityList | activityService |
| Leaderboard | leaderboardService |
| CarbonCredits | statisticsService |
| AdvertisementList / Form | advertisementService |

---

以上即当前前端按「页面」和「跳转」整合后的功能与路由说明。
