# 🔧 修复 Gradle 缓存问题

## ❌ 错误信息
```
Error while executing process jlink.exe
Could not resolve all files for configuration ':app:androidJdkImage'
```

## 💡 原因
Gradle 缓存损坏，导致 `jlink.exe` 执行失败。这是构建系统问题，不是代码错误。

---

## ✅ 解决方案（按顺序尝试）

### 方法 1: Android Studio 清理缓存（最简单）

```
1. 关闭 Android Studio
2. 重新打开 Android Studio
3. File > Invalidate Caches > Invalidate and Restart
4. 等待重启完成
5. Build > Clean Project
6. Build > Rebuild Project
```

### 方法 2: 手动清理 Gradle 缓存

**在 Android Studio Terminal 中执行：**

```bash
# Windows PowerShell
cd C:\Users\csls\Desktop\ad-ui\android-app

# 清理项目
.\gradlew clean --no-daemon

# 停止所有 Gradle daemon
.\gradlew --stop

# 清理 Gradle 缓存
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue

# 重新构建
.\gradlew build --no-daemon
```

### 方法 3: 删除损坏的变换缓存

根据错误信息，问题出在这个缓存：
```
D:\AndroidCache\Gradle\caches\8.12\transforms\8821eb8da9b696bc5f5f53b49975e2f3-...
```

**手动删除：**

```powershell
# 删除整个 Gradle caches 目录
Remove-Item -Path "D:\AndroidCache\Gradle\caches" -Recurse -Force
```

**或者在文件管理器中：**
```
1. 打开文件管理器
2. 导航到: D:\AndroidCache\Gradle\
3. 删除 caches 文件夹
4. 重新在 Android Studio 中 Build > Rebuild Project
```

### 方法 4: 更新 Gradle 版本

编辑 `gradle/wrapper/gradle-wrapper.properties`:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.12-bin.zip
```

改为最新版本：
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip
```

### 方法 5: 检查 JDK 路径

确保 Android Studio 使用正确的 JDK：

```
1. File > Project Structure > SDK Location
2. JDK location 应该是: 
   D:\Program Files\Android\Android Studio\jbr
3. 如果不是，点击 ... 选择正确的路径
4. 点击 OK
5. File > Sync Project with Gradle Files
```

---

## 🚀 推荐的完整流程

**最简单有效的方法：**

```
1. 关闭 Android Studio
2. 手动删除: D:\AndroidCache\Gradle\caches
3. 手动删除: C:\Users\csls\.gradle\caches
4. 重新打开 Android Studio
5. File > Invalidate Caches > Invalidate and Restart
6. 等待重启
7. Build > Clean Project
8. Build > Rebuild Project
9. Run ▶️
```

---

## 📍 缓存位置

您系统上的 Gradle 缓存位置：

| 缓存类型 | 路径 |
|---------|------|
| 用户缓存 | `C:\Users\csls\.gradle\caches\` |
| Android缓存 | `D:\AndroidCache\Gradle\caches\` |
| 项目缓存 | `C:\Users\csls\Desktop\ad-ui\android-app\.gradle\` |

**全部删除命令：**
```powershell
# 停止 Gradle daemon
cd C:\Users\csls\Desktop\ad-ui\android-app
.\gradlew --stop

# 删除所有缓存
Remove-Item -Path "C:\Users\csls\.gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "D:\AndroidCache\Gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "C:\Users\csls\Desktop\ad-ui\android-app\.gradle" -Recurse -Force -ErrorAction SilentlyContinue

# 重新构建
.\gradlew clean build
```

---

## ⚠️ 注意事项

1. **首次重建会很慢**：删除缓存后，Gradle 需要重新下载所有依赖
2. **需要网络连接**：确保网络畅通
3. **磁盘空间**：确保至少有 5GB 可用空间
4. **不要在构建过程中关闭 Android Studio**

---

## 🔍 验证修复

成功的标志：
```
✅ BUILD SUCCESSFUL in XXs
✅ 没有 jlink.exe 错误
✅ 项目可以正常编译
```

---

## 🆘 如果还是失败

如果所有方法都不行：

1. **检查 Android Studio 版本**
   - Help > About
   - 确保是最新稳定版

2. **检查 JDK 版本**
   - 应该是 JDK 17
   - Android Studio > Settings > Build, Execution, Deployment > Build Tools > Gradle
   - Gradle JDK: 选择 "Embedded JDK (jbr-17)"

3. **重新安装 Android Studio**
   - 卸载现有版本
   - 删除所有缓存目录
   - 下载最新版本安装

---

## 📚 相关资源

- [Gradle 官方文档](https://docs.gradle.org/current/userguide/directory_layout.html#dir:gradle_user_home)
- [Android Gradle Plugin 版本](https://developer.android.com/build/releases/gradle-plugin)
- [Stack Overflow 类似问题](https://stackoverflow.com/questions/tagged/gradle+android)

---

**现在请尝试方法 1（最简单）**

如果还有问题，请告诉我具体的错误信息！
