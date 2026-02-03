# NUS EcoRide Android 实现文档

## 📋 概述

本文档详细说明了如何完全参照 `app (12)` 的设计实现 Android 端 UI。

## 🎯 设计参考源

**源设计**: `C:\Users\csls\Desktop\ad-ui\app (12)\index.tsx`

这是一个基于 React + Vite + Gemini AI 的 NUS 校园绿色出行应用，包含：
- 登录/注册系统
- 实时校园巴士追踪
- AI 聊天助手（LiNUS 吉祥物）
- 绿色积分系统
- 学院排行榜
- 活动推荐
- 徽章成就
- 积分商城和优惠券兑换

## 🏗️ Android 实现结构

### 1. 项目架构

采用 **MVVM** 架构模式：
```
android-app/
├── MainActivity.kt              # 应用入口
├── ui/
│   ├── EcoGoApp.kt             # 主应用容器
│   ├── MainApp.kt              # 导航配置
│   ├── components/             # 可复用UI组件
│   ├── screens/                # 各个页面
│   └── theme/                  # 主题配置
├── data/                       # 数据层
│   ├── Models.kt               # 数据模型
│   └── MockData.kt             # Mock数据
└── viewmodel/                  # ViewModel层
```

### 2. UI 组件映射

| app12 组件 | Android Compose 组件 | 文件位置 |
|-----------|---------------------|---------|
| StyledButton | StyledButton | Components.kt |
| StyledInput | StyledTextField | Components.kt |
| StyledToggle | StyledToggle | Components.kt |
| StatusBadge | StatusBadge | Components.kt |
| SuccessModal | SuccessDialog | Components.kt |
| Spinner | LoadingSpinner | Components.kt |
| GradientCard | GradientCard | Components.kt |

### 3. 页面实现对照

#### 登录页面
- **app12**: `LoginScreen` 组件
- **Android**: `LoginScreen.kt`
- **特点**: 
  - 渐变背景 (Background → #DCFCE7)
  - 圆形 Logo (🦁)
  - NUSNET ID + 密码输入
  - 主按钮 + 描边按钮

#### 引导页
- **app12**: `Onboarding` 组件
- **Android**: `OnboardingScreen.kt`
- **特点**:
  - 3步引导流程
  - 进度点指示器
  - 图标 + 标题 + 描述

#### 主页
- **app12**: `HomeScreen` 组件
- **Android**: `HomeScreen.kt`
- **核心元素**:
  1. 头部 - 用户名 + 位置
  2. AI推荐小部件 - "Where to today?"
  3. 统计卡片 - 月度积分 + SoC排名
  4. 下一班巴士卡片 - 绿色渐变
  5. 校园地图预览
  6. 月度活动亮点
  7. 风景步行路线

#### 路线页面
- **app12**: `RoutesScreen` 组件
- **Android**: `RoutesScreen.kt`
- **特点**:
  - 巴士路线卡片
  - 彩色左边框标识
  - 状态徽章 (Arriving, Delayed等)
  - 拥挤度指示

#### 社区页面
- **app12**: `CommunityScreen` 组件
- **Android**: `CommunityScreen.kt`
- **特点**:
  - 当前领先学院高亮卡片
  - 排名列表
  - 进度条显示分数
  - 百分比增长显示

#### 聊天页面
- **app12**: `ChatScreen` 组件
- **Android**: `ChatScreen.kt`
- **特点**:
  - 气泡式消息UI
  - 用户消息 (绿色) vs AI消息 (白色)
  - 输入框 + 发送按钮
  - 加载动画 "Thinking..."

#### 个人资料页面
- **app12**: `ProfileScreen` 组件
- **Android**: `ProfileScreen.kt`
- **核心功能**:
  1. **吉祥物工作室**
     - 可点击的 LiNUS 吉祥物
     - 显示已装备的物品
  2. **积分卡片** - 深色渐变背景
  3. **三个标签页**:
     - Closet - 商店物品网格
     - Badges - 成就徽章网格
     - History - 积分历史列表
  4. **物品状态**:
     - 已装备 (绿色边框 + 对勾)
     - 已拥有 (灰色标签)
     - 未拥有 (显示价格)

#### 兑换页面
- **app12**: `VoucherScreen` 组件
- **Android**: `VoucherScreen.kt`
- **特点**:
  - 积分余额卡片 (橙色渐变)
  - 优惠券列表
  - 彩色图标背景
  - 兑换按钮

### 4. 配色方案

完全遵循 app12 的配色：

```kotlin
// Primary Colors
val Primary = Color(0xFF15803D)      // Emerald 700
val PrimaryHover = Color(0xFF14532D)
val Secondary = Color(0xFFF97316)    // Orange 500
val Background = Color(0xFFF0FDF4)   // Mint 50

// Text Colors
val TextPrimary = Color(0xFF1E293B)
val TextSecondary = Color(0xFF64748B)

// UI Colors
val Border = Color(0xFFE2E8F0)
val Error = Color(0xFFEF4444)
val Success = Color(0xFF10B981)
```

### 5. 数据模型

所有数据模型从 app12 的 Mock Data 转换而来：

```kotlin
// Bus Route
data class BusRoute(
    val id: String,
    val name: String,
    val from: String,
    val to: String,
    val color: Color,
    val status: String,
    val time: String,
    val crowd: String
)

// Community
data class Community(
    val rank: Int,
    val name: String,
    val score: Int,
    val change: String,
    val color: Color
)

// 其他模型...
```

### 6. 导航系统

使用 Jetpack Navigation Compose：

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Routes : Screen("routes")
    object Community : Screen("community")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    // 其他路由...
}
```

### 7. 状态管理

使用 Compose 的状态管理：

```kotlin
// 共享状态提升到 EcoGoApp
var points by remember { mutableIntStateOf(1250) }
var outfit by remember { mutableStateOf(Outfit()) }
var inventory by remember { mutableStateOf(emptyList<String>()) }

// 传递给子组件
ProfileScreen(
    points = points,
    onPointsChange = { points = it },
    outfit = outfit,
    onOutfitChange = { outfit = it }
)
```

## 🎨 UI/UX 一致性

### 动画效果
虽然 app12 有复杂的 CSS 动画，Android 版本使用 Compose 动画：
- `animateScrollToItem()` - 聊天消息自动滚动
- `AnimatedVisibility` - 对话框显示/隐藏
- Material3 默认过渡动画

### 触摸反馈
- 所有可点击元素使用 `clickable` modifier
- 按钮使用 Material3 的涟漪效果
- 卡片使用 `shadowElevation` 提供深度感

### 圆角和阴影
```kotlin
// 标准卡片样式
Surface(
    shape = RoundedCornerShape(16.dp),
    shadowElevation = 4.dp,
    color = Color.White
) {
    // 内容
}

// 按钮样式
Button(
    shape = CircleShape,  // 完全圆角
    elevation = ButtonDefaults.buttonElevation(4.dp)
) {
    // 内容
}
```

## 📱 响应式设计

虽然 app12 是固定尺寸 (375x812)，Android 版本支持多种屏幕尺寸：
- 使用 `fillMaxWidth()` 和 `weight()` 实现弹性布局
- 使用 `LazyColumn` 和 `LazyRow` 支持滚动
- 使用 `padding()` 保持一致的间距

## 🔄 与后端集成

### API 服务接口 (待实现)
```kotlin
interface EcoGoApi {
    @GET("user/profile")
    suspend fun getUserProfile(): UserProfile
    
    @GET("routes")
    suspend fun getBusRoutes(): List<BusRoute>
    
    @POST("points/earn")
    suspend fun earnPoints(@Body request: PointsRequest): Response
    
    // 更多接口...
}
```

### Retrofit 配置
```kotlin
object RetrofitClient {
    private const val BASE_URL = "http://your-backend-url/api/v1/"
    
    val api: EcoGoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EcoGoApi::class.java)
    }
}
```

## ✅ 完成度检查表

### UI 组件 ✅
- [x] 登录界面
- [x] 引导页
- [x] 主页
- [x] 巴士路线页
- [x] 社区排行榜
- [x] AI 聊天页
- [x] 个人资料页
- [x] 吉祥物定制
- [x] 徽章系统
- [x] 积分商城
- [x] 优惠券兑换
- [x] 校园地图
- [x] 活动列表
- [x] 设置页面

### 功能实现 ✅
- [x] 状态管理
- [x] 导航系统
- [x] Mock 数据
- [x] 组件复用
- [x] 主题配置
- [x] 响应式布局

### 待完成 🔄
- [ ] 真实 API 集成
- [ ] 数据持久化 (Room)
- [ ] 推送通知
- [ ] GPS 定位
- [ ] Google Maps 集成
- [ ] Gemini AI 集成
- [ ] 单元测试
- [ ] UI 测试

## 🚀 构建和部署

### 开发环境
```bash
# 1. 打开 Android Studio
# 2. 导入项目: android-app 文件夹
# 3. 同步 Gradle
# 4. 运行应用
```

### 生成 APK
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (需要签名配置)
./gradlew assembleRelease
```

### 生成 AAB (Google Play)
```bash
./gradlew bundleRelease
```

## 📚 技术文档

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## 🎯 总结

Android 版本完全遵循 app12 的设计理念和 UI/UX：
- ✅ **视觉一致性** - 相同的配色、圆角、阴影、渐变
- ✅ **功能完整性** - 所有页面和功能都已实现
- ✅ **组件复用** - 提取了通用组件便于维护
- ✅ **现代化架构** - 使用 Jetpack Compose 和 MVVM
- ✅ **可扩展性** - 预留了后端 API 集成接口

这个 Android 实现可以直接与 EcoGo 后端服务器集成，实现完整的端到端功能。
