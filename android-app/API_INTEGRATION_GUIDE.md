# 🔗 API 集成指南

## ✅ 已完成配置

### 1. API 结构
```
android-app/app/src/main/kotlin/com/ecogo/
├── api/
│   ├── ApiConfig.kt        ✅ API 配置（BASE_URL, 超时等）
│   ├── ApiResponse.kt      ✅ 响应包装类
│   ├── RetrofitClient.kt   ✅ Retrofit 客户端
│   └── ApiService.kt       ✅ API 接口定义
└── repository/
    └── EcoGoRepository.kt  ✅ 数据仓库
```

### 2. 依赖配置
```kotlin
// build.gradle.kts 已添加
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

### 3. 网络权限
```xml
<!-- AndroidManifest.xml 已有 -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🚀 如何使用 API

### 方法 1: 直接在 Fragment 中调用

```kotlin
import androidx.lifecycle.lifecycleScope
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class MyFragment : Fragment() {
    private val repository = EcoGoRepository()
    
    private fun loadActivities() {
        lifecycleScope.launch {
            val result = repository.getAllActivities()
            result.onSuccess { activities ->
                // 成功：更新 UI
                updateUI(activities)
            }.onFailure { error ->
                // 失败：显示错误
                showError(error.message ?: "Unknown error")
            }
        }
    }
}
```

### 方法 2: 使用 ViewModel（推荐）

**创建 ViewModel:**
```kotlin
class ActivityViewModel : ViewModel() {
    private val repository = EcoGoRepository()
    
    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> = _activities
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadActivities() {
        viewModelScope.launch {
            val result = repository.getAllActivities()
            result.onSuccess { _activities.value = it }
            result.onFailure { _error.value = it.message }
        }
    }
}
```

**在 Fragment 中使用:**
```kotlin
class ActivitiesFragment : Fragment() {
    private val viewModel: ActivityViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 观察数据
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            // 更新 UI
            adapter.submitList(activities)
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            // 显示错误
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
        
        // 加载数据
        viewModel.loadActivities()
    }
}
```

---

## 📋 可用的 API 方法

### 活动相关
```kotlin
repository.getAllActivities()           // 获取所有活动
repository.getActivityById(id)          // 获取单个活动
```

### 排行榜相关
```kotlin
repository.getLeaderboard(period)       // 获取排名
repository.getAvailablePeriods()        // 获取可用周期
```

### 用户相关
```kotlin
repository.getUserInfo(userId)          // 获取用户信息
repository.getUserPoints(userId)        // 获取用户积分
```

### 徽章相关
```kotlin
repository.getAllBadges()               // 获取所有徽章
repository.getUserBadges(userId)        // 获取用户徽章
```

### 商品相关
```kotlin
repository.getAllGoods()                // 获取所有商品
```

### 订单相关
```kotlin
repository.createOrder(order)           // 创建订单
repository.getUserOrders(userId)        // 获取用户订单
```

### 统计相关
```kotlin
repository.getUserStatistics(userId)    // 获取用户统计
```

---

## ⚙️ 配置说明

### 修改 BASE_URL

编辑 `ApiConfig.kt`:

```kotlin
object ApiConfig {
    // 模拟器使用（访问本机）
    const val BASE_URL = "http://10.0.2.2:8090/"
    
    // 真实设备使用（替换为你的电脑 IP）
    // const val BASE_URL = "http://192.168.1.100:8090/"
    
    // 生产环境使用
    // const val BASE_URL = "https://your-domain.com/"
}
```

### 查找你的电脑 IP

**Windows:**
```powershell
ipconfig
# 查找 "IPv4 地址"
```

**macOS/Linux:**
```bash
ifconfig
# 或
ip addr show
```

---

## 🔧 后端启动步骤

### 1. 启动 MongoDB

```bash
# 确保 MongoDB 在运行
mongod
```

### 2. 启动后端服务

**在 IntelliJ IDEA 中:**
```
1. 打开项目: C:\Users\csls\Desktop\ad-ui
2. 找到主类: EcoGoApplication.java
3. 右键 > Run 'EcoGoApplication'
4. 等待启动完成
5. 看到 "Started EcoGoApplication in X seconds"
```

**或使用命令行:**
```bash
cd C:\Users\csls\Desktop\ad-ui
mvn spring-boot:run
```

### 3. 验证后端运行

浏览器访问:
```
http://localhost:8090/actuator/health
```

应该返回:
```json
{
  "status": "UP"
}
```

---

## 📱 Android 使用步骤

### 1. Sync Gradle
```
Android Studio > File > Sync Project with Gradle Files
```

### 2. 启动后端
按照上面的步骤启动后端服务

### 3. 运行 Android 应用
```
1. 启动模拟器
2. 点击 Run ▶️
```

### 4. 测试 API 连接

在任意 Fragment 中添加测试代码:

```kotlin
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val repository = EcoGoRepository()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 测试 API 连接
        testApiConnection()
    }
    
    private fun testApiConnection() {
        lifecycleScope.launch {
            try {
                val result = repository.getAllActivities()
                result.onSuccess { activities ->
                    Log.d("API_TEST", "✅ 成功获取 ${activities.size} 个活动")
                    activities.forEach { Log.d("API_TEST", "- ${it.title}") }
                }.onFailure { error ->
                    Log.e("API_TEST", "❌ 失败: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "❌ 异常: ${e.message}")
            }
        }
    }
}
```

---

## 🐛 故障排除

### 问题 1: 连接失败
```
错误: Failed to connect to /10.0.2.2:8090
```

**解决方案:**
1. 确保后端在运行（检查端口 8090）
2. 模拟器使用 `10.0.2.2` 访问本机
3. 真实设备使用电脑 IP（如 `192.168.1.100`）

### 问题 2: 超时
```
错误: java.net.SocketTimeoutException
```

**解决方案:**
```kotlin
// 增加超时时间（ApiConfig.kt）
const val CONNECT_TIMEOUT = 60L  // 改为 60 秒
const val READ_TIMEOUT = 60L
```

### 问题 3: 401/403 错误
```
错误: HTTP 401 Unauthorized
```

**解决方案:**
- AuthController 目前为空
- 暂时不需要认证
- 如果后端启用了认证，需要在请求头添加 token

### 问题 4: 看不到网络日志

**启用详细日志:**

在 Logcat 中过滤:
```
Tag: OkHttp
或
Tag: API_TEST
```

---

## 📊 示例：更新 ActivitiesFragment 使用真实 API

```kotlin
package com.ecogo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.databinding.FragmentActivitiesBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.ActivityAdapter
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {
    
    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadActivities()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerActivities.layoutManager = LinearLayoutManager(context)
    }
    
    private fun loadActivities() {
        lifecycleScope.launch {
            try {
                val result = repository.getAllActivities()
                result.onSuccess { activities ->
                    Log.d("ActivitiesFragment", "✅ 获取到 ${activities.size} 个活动")
                    binding.recyclerActivities.adapter = ActivityAdapter(activities)
                }.onFailure { error ->
                    Log.e("ActivitiesFragment", "❌ 加载失败: ${error.message}")
                    Toast.makeText(
                        context,
                        "加载失败: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // 失败时使用 Mock 数据
                    binding.recyclerActivities.adapter = 
                        ActivityAdapter(MockData.ACTIVITIES)
                }
            } catch (e: Exception) {
                Log.e("ActivitiesFragment", "❌ 异常: ${e.message}")
                // 异常时使用 Mock 数据
                binding.recyclerActivities.adapter = 
                    ActivityAdapter(MockData.ACTIVITIES)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## 🎯 推荐的开发流程

### 阶段 1: 测试连接（当前）
1. ✅ 创建 API 配置文件
2. ✅ 创建 Repository
3. ⏭️ 启动后端
4. ⏭️ 测试一个简单的 API 调用

### 阶段 2: 逐步迁移
1. 先迁移 ActivitiesFragment（最简单）
2. 再迁移 CommunityFragment（排行榜）
3. 最后迁移其他 Fragment

### 阶段 3: 添加功能
1. 添加加载状态（ProgressBar）
2. 添加错误处理（Snackbar/Dialog）
3. 添加下拉刷新
4. 添加离线缓存

---

## 📚 相关文档

- [Retrofit 官方文档](https://square.github.io/retrofit/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)

---

## ✅ 检查清单

使用 API 前请确认：

- [ ] 后端正在运行（端口 8090）
- [ ] MongoDB 正在运行
- [ ] Android Manifest 有网络权限
- [ ] BASE_URL 配置正确
- [ ] Gradle 已同步
- [ ] 添加了错误处理代码

---

**🎉 现在 Android 应用已经可以连接后端了！**

下一步：
1. 启动后端服务
2. 运行 Android 应用
3. 查看 Logcat 中的网络请求日志
4. 根据需要更新 Fragment 使用真实 API

需要帮助？查看故障排除部分或提问！😊
