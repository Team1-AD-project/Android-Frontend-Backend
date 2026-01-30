# 🚀 快速启动指南

## 5分钟运行应用

### 前提条件 ✅
- [ ] 已安装 Android Studio (Hedgehog 2023.1.1+)
- [ ] 已安装 JDK 17
- [ ] 有 Android 设备或模拟器

### 步骤 1️⃣: 打开项目
```
1. 启动 Android Studio
2. 选择 "Open" 
3. 导航到: C:\Users\csls\Desktop\ad-ui\android-app
4. 点击 "OK"
```

### 步骤 2️⃣: 同步 Gradle (自动)
```
Android Studio 会自动开始同步
等待底部状态栏显示 "Gradle sync finished"
```

如果没有自动同步：
```
File > Sync Project with Gradle Files
```

### 步骤 3️⃣: 启动模拟器
```
Tools > Device Manager
点击播放图标启动一个现有模拟器
或者点击 "Create Device" 创建新的
```

推荐配置：
- Device: Pixel 7
- System Image: API 34 (Android 14)
- RAM: 2048 MB

### 步骤 4️⃣: 运行应用
```
点击工具栏的绿色运行按钮 ▶️
或者按 Shift+F10
```

### 🎉 完成！
应用会在几秒钟内启动，你会看到登录界面。

## 🐛 常见问题

### Q1: Gradle sync 失败
**解决方案**:
```
1. 检查网络连接
2. File > Invalidate Caches > Invalidate and Restart
3. 删除 .gradle 文件夹后重新同步
```

### Q2: 找不到 JDK
**解决方案**:
```
File > Project Structure > SDK Location
确保 JDK location 指向 JDK 17
```

### Q3: 模拟器启动失败
**解决方案**:
```
1. 检查 BIOS 中是否启用了虚拟化 (VT-x/AMD-V)
2. 确保有足够的磁盘空间 (至少 8GB)
3. 尝试使用物理设备
```

### Q4: 构建错误
**解决方案**:
```
1. Build > Clean Project
2. Build > Rebuild Project
3. 检查 gradle.properties 中的内存设置
```

## 📱 使用物理设备

### 启用开发者选项
```
1. 打开手机设置
2. 关于手机
3. 连续点击"版本号" 7次
4. 返回设置 > 开发者选项
5. 启用 "USB 调试"
```

### 连接设备
```
1. 用 USB 数据线连接手机和电脑
2. 手机上允许 USB 调试
3. 在 Android Studio 中选择你的设备
4. 点击运行 ▶️
```

## 🎮 测试功能

### 默认登录
```
随意输入任何 NUSNET ID 和密码
点击 "Sign In"
```

### 导航测试
```
1. 底部导航栏切换页面
2. 点击主页上的 "Open Map"
3. 点击活动卡片
4. 尝试聊天功能
```

### 积分系统测试
```
1. 进入 Profile 页面
2. 查看当前积分: 1250 pts
3. 点击 Closet 标签
4. 购买物品 (如: Orange Cap - 200 pts)
5. 查看吉祥物变化
```

### 优惠券兑换测试
```
1. Profile 页面点击 "Redeem"
2. 选择优惠券
3. 点击兑换按钮
4. 查看积分变化
```

## 🔧 开发模式

### 热重载
Compose 支持实时预览：
```
@Preview
@Composable
fun PreviewLoginScreen() {
    LoginScreen(onLogin = {}, onSignUp = {})
}
```

### 调试
```
1. 在代码行号左侧点击设置断点
2. 点击调试按钮 🐛
3. 应用会在断点处暂停
```

### 日志查看
```
View > Tool Windows > Logcat
过滤标签: "EcoGo"
```

## 📦 生成 APK

### Debug APK
```bash
cd C:\Users\csls\Desktop\ad-ui\android-app
./gradlew assembleDebug
```

输出位置:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 安装到设备
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🎨 自定义

### 修改主题颜色
```kotlin
// 文件: ui/theme/Color.kt
val Primary = Color(0xFF15803D)  // 改成你想要的颜色
```

### 修改应用名称
```xml
<!-- 文件: res/values/strings.xml -->
<string name="app_name">你的应用名</string>
```

### 添加新页面
```kotlin
// 1. 在 ui/screens/ 创建新文件
@Composable
fun MyNewScreen() {
    // UI 代码
}

// 2. 在 MainApp.kt 添加路由
composable("myscreen") {
    MyNewScreen()
}

// 3. 导航到新页面
navController.navigate("myscreen")
```

## 📚 推荐阅读

- [Compose 布局基础](https://developer.android.com/jetpack/compose/layouts/basics)
- [状态管理](https://developer.android.com/jetpack/compose/state)
- [Navigation](https://developer.android.com/jetpack/compose/navigation)

## 💡 小技巧

### 快捷键
- `Ctrl + Space`: 代码补全
- `Ctrl + Shift + F`: 全局搜索
- `Ctrl + Alt + L`: 格式化代码
- `Shift + F10`: 运行应用
- `Shift + F9`: 调试应用

### Compose 技巧
```kotlin
// 1. 使用 remember 避免重组
var count by remember { mutableStateOf(0) }

// 2. 使用 LazyColumn 优化列表
LazyColumn {
    items(list) { item ->
        ItemCard(item)
    }
}

// 3. 提取可复用组件
@Composable
fun MyButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text)
    }
}
```

## 🎯 下一步

现在你已经成功运行了应用！接下来可以：

1. 📖 阅读 README.md 了解项目详情
2. 🔍 浏览代码了解实现细节
3. 🎨 尝试修改 UI 组件
4. 🔌 集成后端 API
5. 📱 添加新功能

**祝你开发愉快！** 🚀
