# NUS EcoRide - Android App

这是 NUS EcoRide 应用的 Android 版本，使用 **Jetpack Compose** 构建，完全参考了 app12 的设计。

## 📱 应用功能

### 核心功能
- ✅ **登录/注册** - NUSNET ID 登录系统
- ✅ **引导页** - 首次使用引导
- ✅ **实时巴士追踪** - 显示校园巴士实时位置和到达时间
- ✅ **绿色积分系统** - 通过环保出行赚取积分
- ✅ **AI聊天助手** - LiNUS吉祥物提供校园导航帮助
- ✅ **校园地图** - 互动式校园地图
- ✅ **学院排行榜** - 各学院绿色出行竞赛
- ✅ **活动推荐** - 校园环保活动信息
- ✅ **徽章成就系统** - 解锁各种成就徽章
- ✅ **积分商城** - 自定义吉祥物外观
- ✅ **优惠券兑换** - 使用积分兑换校园优惠券

### 页面列表
1. **主页 (HomeScreen)** - 下一班巴士、推荐路线、活动预览
2. **路线页 (RoutesScreen)** - 所有巴士路线实时状态
3. **社区页 (CommunityScreen)** - 学院排行榜
4. **聊天页 (ChatScreen)** - 与 LiNUS 对话
5. **个人资料页 (ProfileScreen)** - 积分、徽章、历史记录、吉祥物自定义
6. **地图页 (MapScreen)** - 校园地图
7. **活动页 (ActivitiesScreen)** - 校园活动列表
8. **兑换页 (VoucherScreen)** - 积分兑换优惠券
9. **设置页 (SettingsScreen)** - 应用设置

## 🛠️ 技术栈

- **Kotlin** - 编程语言
- **Jetpack Compose** - 现代化 UI 框架
- **Material Design 3** - UI 设计系统
- **Navigation Compose** - 页面导航
- **Coroutines** - 异步编程
- **ViewModel** - MVVM 架构

## 🎨 设计特点

完全参照 app12 (React) 的设计实现：

- **配色方案**
  - Primary: Emerald 700 (#15803D)
  - Secondary: Orange 500 (#F97316)
  - Background: Mint 50 (#F0FDF4)

- **UI 元素**
  - 圆角卡片设计
  - 渐变色按钮和卡片
  - 流畅的动画过渡
  - 现代化的图标系统

## 📦 项目结构

```
android-app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/com/ecogo/
│   │       │   ├── MainActivity.kt          # 应用入口
│   │       │   ├── ui/
│   │       │   │   ├── EcoGoApp.kt         # 主应用组件
│   │       │   │   ├── MainApp.kt          # 导航配置
│   │       │   │   ├── components/         # 通用UI组件
│   │       │   │   │   └── Components.kt
│   │       │   │   ├── screens/            # 各个页面
│   │       │   │   │   ├── LoginScreen.kt
│   │       │   │   │   ├── OnboardingScreen.kt
│   │       │   │   │   ├── HomeScreen.kt
│   │       │   │   │   ├── RoutesScreen.kt
│   │       │   │   │   ├── CommunityScreen.kt
│   │       │   │   │   ├── ChatScreen.kt
│   │       │   │   │   ├── ProfileScreen.kt
│   │       │   │   │   ├── MapScreen.kt
│   │       │   │   │   ├── ActivitiesScreen.kt
│   │       │   │   │   ├── VoucherScreen.kt
│   │       │   │   │   └── SettingsScreen.kt
│   │       │   │   └── theme/              # 主题配置
│   │       │   │       ├── Color.kt
│   │       │   │       ├── Theme.kt
│   │       │   │       └── Type.kt
│   │       │   ├── data/                   # 数据模型
│   │       │   │   ├── Models.kt
│   │       │   │   └── MockData.kt
│   │       │   └── viewmodel/              # ViewModel层
│   │       ├── res/                        # 资源文件
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 🚀 构建和运行

### 前置要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK API 34
- Kotlin 1.9.20+

### 步骤

1. **克隆或打开项目**
   ```bash
   # 在 Android Studio 中打开 android-app 文件夹
   ```

2. **同步 Gradle**
   - Android Studio 会自动提示同步
   - 或者点击 `File > Sync Project with Gradle Files`

3. **运行应用**
   - 连接 Android 设备或启动模拟器 (API 26+)
   - 点击运行按钮 ▶️ 或按 Shift+F10

### 模拟器推荐设置
- Device: Pixel 6 或 Pixel 7
- API Level: 34 (Android 14)
- RAM: 2048 MB

## 🔗 连接后端 API

要连接到 EcoGo 后端服务器：

1. 修改 `app/src/main/kotlin/com/ecogo/data/ApiService.kt` 中的 BASE_URL
2. 更新为你的后端服务器地址（例如：`http://10.0.2.2:8080/api/v1/` 用于模拟器）
3. 确保后端服务器正在运行

```kotlin
object ApiConfig {
    const val BASE_URL = "http://your-backend-url/api/v1/"
}
```

## 📝 Mock 数据

当前应用使用 Mock 数据进行演示。所有数据定义在：
- `com.ecogo.data.Models.kt` - 数据模型
- `com.ecogo.data.MockData.kt` - 模拟数据

## 🎯 与 app12 的一致性

| 功能 | app12 (React) | Android | 状态 |
|------|--------------|---------|------|
| 登录界面 | ✅ | ✅ | 完成 |
| 引导页 | ✅ | ✅ | 完成 |
| 主页 | ✅ | ✅ | 完成 |
| 巴士路线 | ✅ | ✅ | 完成 |
| 社区排行榜 | ✅ | ✅ | 完成 |
| AI聊天 | ✅ | ✅ | 完成 |
| 个人资料 | ✅ | ✅ | 完成 |
| 吉祥物定制 | ✅ | ✅ | 完成 |
| 徽章系统 | ✅ | ✅ | 完成 |
| 积分商城 | ✅ | ✅ | 完成 |
| 优惠券兑换 | ✅ | ✅ | 完成 |
| 校园地图 | ✅ | ✅ | 完成 |
| 活动列表 | ✅ | ✅ | 完成 |
| 设置页面 | ✅ | ✅ | 完成 |

## 🔄 后续增强

- [ ] 集成真实的后端 API
- [ ] 添加 Google Maps 集成
- [ ] 实现真实的 AI 聊天（使用 Gemini API）
- [ ] 添加推送通知
- [ ] 实现 GPS 位置追踪
- [ ] 添加离线模式
- [ ] 实现数据持久化（Room Database）
- [ ] 添加单元测试和 UI 测试

## 📄 许可证

此项目仅用于教育和演示目的。

## 👥 贡献

欢迎贡献！请随时提交 Pull Request。

## 📧 联系方式

如有问题或建议，请联系开发团队。
