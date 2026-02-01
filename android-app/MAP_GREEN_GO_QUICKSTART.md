# Map Green Go 快速启动指南

## 前置要求

1. **Android Studio**: Arctic Fox 或更高版本
2. **JDK**: Java 17
3. **Google Maps API Key**: [获取API Key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

## 配置步骤

### 1. 获取 Google Maps API Key

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建新项目或选择现有项目
3. 启用以下API：
   - Maps SDK for Android
   - Directions API (可选)
   - Places API (可选)
4. 创建凭据 → API密钥
5. 限制API密钥（推荐）：
   - 应用限制：Android应用
   - 添加包名：`com.ecogo`
   - 添加SHA-1指纹

### 2. 配置 API Key

打开 `android-app/app/src/main/AndroidManifest.xml`，找到：

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE" />
```

将 `YOUR_GOOGLE_MAPS_API_KEY_HERE` 替换为你的实际API Key。

**安全提示**: 不要将API Key提交到公开的Git仓库！建议使用以下方式之一：

#### 方法1: 使用local.properties（推荐）
```properties
# local.properties
MAPS_API_KEY=你的API_KEY
```

然后在 build.gradle.kts 中读取：
```kotlin
android {
    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] = 
            project.findProperty("MAPS_API_KEY") ?: "default_key"
    }
}
```

AndroidManifest.xml 改为：
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

### 3. 同步项目

1. 打开 Android Studio
2. 打开项目: `File → Open → 选择 android-app 文件夹`
3. 等待 Gradle 同步完成
4. 如果遇到依赖问题，点击 `File → Sync Project with Gradle Files`

### 4. 运行应用

1. 连接 Android 设备或启动模拟器（API 24+）
2. 点击 `Run → Run 'app'`
3. 应用会安装并启动

## 使用 Map Green Go 功能

### 访问导航功能

1. **登录应用** (如果首次启动)
2. 点击底部导航栏的 **"Green Go"** 图标 (📍)
3. 你会看到包含 Google 地图的主界面

### 搜索地点

1. 点击顶部的搜索框
2. 输入地点名称（例如："COM1", "UTown"）
3. 从列表中选择地点

### 规划路线

1. 点击"规划路线"或搜索框
2. 选择起点（默认为当前位置）
3. 选择终点
4. 选择交通方式：
   - 🚶 步行 (0g CO₂)
   - 🚲 骑行 (0g CO₂)
   - 🚌 公交 (50g CO₂/km)
5. 查看路线选项和碳排放对比
6. 点击"开始导航"

### 查看碳排放数据

每个路线选项都会显示：
- ☁️ CO₂排放量
- 🌱 绿色积分
- 💰 节省金额

## 测试数据

应用内置了以下测试地点：

| 地点 | 类型 | 坐标 |
|------|------|------|
| COM1 | 学院 | 1.2948, 103.7743 |
| UTown | 设施 | 1.3036, 103.7739 |
| Central Library | 图书馆 | 1.2966, 103.7723 |
| PGP Residence | 宿舍 | 1.2913, 103.7803 |
| The Deck | 食堂 | 1.2993, 103.7710 |
| Kent Ridge MRT | 公交站 | 1.2931, 103.7843 |

## 故障排除

### 地图不显示

**问题**: 地图显示空白或灰色网格

**解决方案**:
1. 检查 API Key 是否正确配置
2. 确认已启用 Maps SDK for Android
3. 检查 AndroidManifest.xml 中的权限
4. 查看 Logcat 中的错误信息

```bash
# 过滤地图相关日志
adb logcat | grep -i "maps\|google\|location"
```

### 无法获取位置

**问题**: 无法获取当前位置

**解决方案**:
1. 在设备设置中启用位置服务
2. 授予应用位置权限（设置 → 应用 → EcoRide → 权限）
3. 在模拟器中设置模拟位置：
   - Extended Controls (三个点) → Location
   - 输入坐标：1.2966, 103.7764 (NUS中心)

### 编译错误

**问题**: Gradle 同步失败

**解决方案**:
```bash
# 清理项目
./gradlew clean

# 重新构建
./gradlew build

# 如果还有问题，删除缓存
rm -rf .gradle
rm -rf app/build
```

### ViewBinding 错误

**问题**: Cannot resolve symbol 'databinding'

**解决方案**:
1. 确认 build.gradle.kts 中已启用 viewBinding:
```kotlin
buildFeatures {
    viewBinding = true
    dataBinding = true
}
```
2. 执行 `Build → Clean Project`
3. 执行 `Build → Rebuild Project`

## 开发调试

### 查看日志

```kotlin
// 在代码中添加日志
import android.util.Log

Log.d("MapGreenGo", "调试信息")
Log.e("MapGreenGo", "错误信息")
```

查看日志：
```bash
adb logcat | grep "MapGreenGo"
```

### 调试导航

1. 在 `MapGreenGoFragment.kt` 中设置断点
2. 点击 Debug (虫子图标) 运行应用
3. 触发导航功能，观察变量值

### 模拟位置移动

在模拟器中测试导航：
1. Extended Controls → Location
2. 选择 "Routes" 选项卡
3. 设置起点和终点
4. 点击 "Play Route" 模拟移动

## 性能优化

### 减少内存使用

```kotlin
// 在 MapGreenGoFragment 中
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // 释放 binding
}
```

### 优化地图加载

```kotlin
// 使用 lite 模式（如果只需要静态地图）
val options = GoogleMapOptions()
    .liteMode(true)
```

## 常见问题 (FAQ)

### Q: API Key 配额用完了怎么办？
A: Google Maps 每月提供 $200 免费额度。如果超出，需要升级账户或优化API调用。

### Q: 支持离线地图吗？
A: 当前版本不支持。可以考虑集成 OpenStreetMap 实现离线功能。

### Q: 如何修改碳排放计算公式？
A: 编辑 `CarbonCalculator.kt` 中的 `CARBON_RATES` 常量。

### Q: 如何添加新的交通方式？
A: 
1. 在 `NavigationModels.kt` 的 `TransportMode` 枚举中添加新类型
2. 在 `CarbonCalculator.kt` 中添加对应的碳排放系数
3. 在 UI 中添加对应的选项

## 下一步

- [ ] 集成真实的 Directions API
- [ ] 实现实时导航界面
- [ ] 添加语音导航
- [ ] 实现行程历史记录
- [ ] 添加社交分享功能
- [ ] 接入后端API

## 需要帮助？

- 查看 [MAP_GREEN_GO_IMPLEMENTATION.md](MAP_GREEN_GO_IMPLEMENTATION.md) 了解详细实现
- 查看 [ANDROID_IMPLEMENTATION.md](ANDROID_IMPLEMENTATION.md) 了解整体架构
- Google Maps Android SDK 文档: https://developers.google.com/maps/documentation/android-sdk

---

**最后更新**: 2026-02-01  
**版本**: v1.0.0
