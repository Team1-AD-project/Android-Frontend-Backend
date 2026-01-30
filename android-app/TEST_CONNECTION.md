# 🧪 API 连接测试指南

## 快速测试步骤

### 步骤 1: 在任意 Fragment 中添加测试代码

在 `ActivitiesFragment.kt` 的 `onViewCreated` 方法中添加：

```kotlin
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {
    
    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 测试 API 连接
        testApiConnection()
    }
    
    private fun testApiConnection() {
        lifecycleScope.launch {
            Log.d("API_TEST", "==========================================")
            Log.d("API_TEST", "🔄 开始测试 API 连接...")
            Log.d("API_TEST", "==========================================")
            
            // 测试 1: 活动列表
            try {
                Log.d("API_TEST", "\n[测试 1] 加载活动列表...")
                val result = repository.getAllActivities()
                result.onSuccess { activities ->
                    Log.d("API_TEST", "✅ 成功！获取到 ${activities.size} 个活动")
                    activities.take(3).forEach {
                        Log.d("API_TEST", "  📌 ${it.title} (${it.status})")
                    }
                }.onFailure { error ->
                    Log.e("API_TEST", "❌ 失败: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "❌ 异常: ${e.message}")
            }
            
            // 测试 2: 排行榜周期
            try {
                Log.d("API_TEST", "\n[测试 2] 获取排行榜周期...")
                val result = repository.getAvailablePeriods()
                result.onSuccess { periods ->
                    Log.d("API_TEST", "✅ 成功！可用周期: ${periods.size} 个")
                    periods.take(3).forEach {
                        Log.d("API_TEST", "  📅 $it")
                    }
                }.onFailure { error ->
                    Log.e("API_TEST", "❌ 失败: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "❌ 异常: ${e.message}")
            }
            
            // 测试 3: 商品列表
            try {
                Log.d("API_TEST", "\n[测试 3] 加载商品列表...")
                val result = repository.getAllGoods()
                result.onSuccess { response ->
                    Log.d("API_TEST", "✅ 成功！获取到 ${response.data.size} 个商品")
                    response.data.take(3).forEach {
                        Log.d("API_TEST", "  🛍️ ${it.name} - ${it.redemptionPoints} 积分")
                    }
                }.onFailure { error ->
                    Log.e("API_TEST", "❌ 失败: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "❌ 异常: ${e.message}")
            }
            
            // 测试 4: 仪表盘统计
            try {
                Log.d("API_TEST", "\n[测试 4] 获取仪表盘统计...")
                val result = repository.getDashboardStats()
                result.onSuccess { stats ->
                    Log.d("API_TEST", "✅ 成功！统计数据:")
                    Log.d("API_TEST", "  👥 总用户: ${stats.totalUsers}")
                    Log.d("API_TEST", "  📊 总活动: ${stats.totalActivities}")
                    Log.d("API_TEST", "  🌿 碳减排: ${stats.totalCarbonReduction}")
                }.onFailure { error ->
                    Log.e("API_TEST", "❌ 失败: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "❌ 异常: ${e.message}")
            }
            
            Log.d("API_TEST", "\n==========================================")
            Log.d("API_TEST", "🏁 测试完成")
            Log.d("API_TEST", "==========================================")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### 步骤 2: Sync Gradle
```
Android Studio > File > Sync Project with Gradle Files
```

### 步骤 3: 启动后端

**在 IntelliJ IDEA 中：**
```
1. 打开项目: C:\Users\csls\Desktop\ad-ui
2. 找到: src/main/java/com/example/EcoGo/EcoGoApplication.java
3. 右键 > Run 'EcoGoApplication'
4. 等待启动完成（看到 "Started EcoGoApplication"）
```

**验证后端运行：**

浏览器访问：`http://localhost:8090/actuator/health`

应该返回：
```json
{
  "status": "UP"
}
```

### 步骤 4: 运行 Android 应用
```
1. 启动模拟器
2. 点击 Run ▶️
3. 打开 Logcat
```

### 步骤 5: 查看 Logcat 日志

在 Logcat 中过滤 `API_TEST`：

**预期输出（成功）：**
```
API_TEST: ==========================================
API_TEST: 🔄 开始测试 API 连接...
API_TEST: ==========================================
API_TEST: 
API_TEST: [测试 1] 加载活动列表...
API_TEST: ✅ 成功！获取到 5 个活动
API_TEST:   📌 校园骑行活动 (PUBLISHED)
API_TEST:   📌 环保讲座 (ONGOING)
API_TEST: 
API_TEST: [测试 2] 获取排行榜周期...
API_TEST: ✅ 成功！可用周期: 4 个
API_TEST:   📅 Week 4, 2026
API_TEST:   📅 Week 3, 2026
API_TEST: 
API_TEST: [测试 3] 加载商品列表...
API_TEST: ✅ 成功！获取到 5 个商品
API_TEST:   🛍️ 环保水杯 - 500 积分
API_TEST:   🛍️ 有机棉T恤 - 800 积分
API_TEST: 
API_TEST: [测试 4] 获取仪表盘统计...
API_TEST: ✅ 成功！统计数据:
API_TEST:   👥 总用户: 1
API_TEST:   📊 总活动: 5
API_TEST:   🌿 碳减排: 12500
API_TEST: 
API_TEST: ==========================================
API_TEST: 🏁 测试完成
API_TEST: ==========================================
```

---

## 🚨 常见错误及解决方案

### 错误 1: `Failed to connect to /10.0.2.2:8090`

**Logcat 输出：**
```
API_TEST: ❌ 失败: Failed to connect to /10.0.2.2:8090
```

**原因：** 后端未启动

**解决方案：**
```
1. 确认后端正在运行（IntelliJ IDEA 控制台有输出）
2. 浏览器访问 http://localhost:8090/actuator/health 验证
3. 如果后端在运行，检查端口是否被占用
```

---

### 错误 2: `HTTP 404 Not Found`

**Logcat 输出：**
```
OkHttp: HTTP 404 Not Found
API_TEST: ❌ 失败: HTTP 404 Not Found
```

**原因：** API 路径错误

**解决方案：**
```
1. 检查 ApiService.kt 中的路径是否正确
2. 对照后端 Controller 的 @RequestMapping
3. 确认后端路径格式: /api/v1/{endpoint}
```

**如何验证：**
```
浏览器或 Postman 测试：
http://localhost:8090/api/v1/activities
```

---

### 错误 3: `Expected BEGIN_OBJECT but was BEGIN_ARRAY`

**Logcat 输出：**
```
JsonSyntaxException: Expected BEGIN_OBJECT but was BEGIN_ARRAY
API_TEST: ❌ 失败: JSON parse error
```

**原因：** 后端返回的 JSON 结构与前端 DTO 不匹配

**解决方案：**
```
1. 查看 OkHttp 日志中的完整响应 JSON
2. 对照 ApiService.kt 中的 DTO 定义
3. 如果后端返回数组，DTO 也应该是 List<T>
```

**如何查看完整 JSON：**

在 Logcat 中过滤 `OkHttp`，找到类似的日志：
```
OkHttp: <-- 200 OK http://10.0.2.2:8090/api/v1/activities
OkHttp: {"code":200,"message":"success","data":[...]}
```

---

### 错误 4: `code: 200, data: null`

**Logcat 输出：**
```
API_TEST: ✅ 成功！获取到 0 个活动
```

**原因：** 后端数据库中没有数据

**解决方案：**
```
1. 检查 MongoDB 是否有数据
2. 使用 Postman POST 创建测试数据
3. 或者在后端添加测试数据种子
```

**Postman 创建活动示例：**
```
POST http://localhost:8090/api/v1/activities
Content-Type: application/json

{
  "title": "测试活动",
  "description": "这是一个测试活动",
  "type": "ONLINE",
  "status": "PUBLISHED",
  "rewardCredits": 100,
  "maxParticipants": 50,
  "startTime": "2026-02-01T10:00:00",
  "endTime": "2026-02-01T16:00:00"
}
```

---

### 错误 5: 网络请求日志看不到

**问题：** Logcat 中没有 OkHttp 日志

**解决方案：**

确认 `build.gradle.kts` 已添加：
```kotlin
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

确认 `RetrofitClient.kt` 已配置：
```kotlin
private val okHttpClient: OkHttpClient by lazy {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
}
```

---

## 📊 查看详细网络日志

### 在 Logcat 中过滤 `OkHttp`

你会看到类似的日志：

```
OkHttp: --> GET http://10.0.2.2:8090/api/v1/activities
OkHttp: --> END GET
OkHttp: 
OkHttp: <-- 200 OK http://10.0.2.2:8090/api/v1/activities (125ms)
OkHttp: Content-Type: application/json
OkHttp: Transfer-Encoding: chunked
OkHttp: 
OkHttp: {"code":200,"message":"success","data":[
OkHttp:   {
OkHttp:     "id":"507f1f77bcf86cd799439011",
OkHttp:     "title":"校园骑行活动",
OkHttp:     "description":"周末骑行活动",
OkHttp:     "type":"OFFLINE",
OkHttp:     "status":"PUBLISHED",
OkHttp:     "rewardCredits":100
OkHttp:   }
OkHttp: ]}
OkHttp: <-- END HTTP
```

---

## 🎯 完整测试流程

### 1. 准备环境
- ✅ Gradle 已同步
- ✅ 后端正在运行（端口 8090）
- ✅ MongoDB 正在运行
- ✅ 模拟器已启动

### 2. 添加测试代码
- ✅ 在 Fragment 中添加 `testApiConnection()` 方法
- ✅ 在 `onViewCreated` 中调用

### 3. 运行并观察
- ✅ 点击 Run ▶️
- ✅ 打开 Logcat
- ✅ 过滤 `API_TEST`

### 4. 分析结果
- ✅ 所有测试通过 → 连接成功，可以开始使用 API
- ❌ 某些测试失败 → 查看错误信息，参考上面的解决方案

---

## 🌐 切换到真实设备测试

如果使用真实 Android 设备（非模拟器）：

### 步骤 1: 查找电脑 IP

**Windows:**
```powershell
ipconfig
# 查找 "IPv4 地址"，如: 192.168.1.100
```

**macOS/Linux:**
```bash
ifconfig
# 或
ip addr show
```

### 步骤 2: 更新 BASE_URL

编辑 `ApiConfig.kt`:

```kotlin
object ApiConfig {
    // 替换为你的电脑 IP
    const val BASE_URL = "http://192.168.1.100:8090/"
}
```

### 步骤 3: 确保防火墙允许

**Windows 防火墙：**
```
1. 控制面板 > Windows Defender 防火墙
2. 高级设置 > 入站规则
3. 新建规则 > 端口 > TCP > 8090
4. 允许连接
```

---

## 🎉 测试成功后的下一步

一旦测试通过，你可以：

### 1. 更新 ActivitiesFragment 使用真实 API
```kotlin
private fun loadActivities() {
    lifecycleScope.launch {
        val result = repository.getAllActivities()
        result.onSuccess { activities ->
            adapter.submitList(activities)
        }.onFailure { error ->
            Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
            // 失败时使用 Mock 数据
            adapter.submitList(MockData.ACTIVITIES)
        }
    }
}
```

### 2. 更新 CommunityFragment 使用真实排行榜
```kotlin
private fun loadLeaderboard() {
    lifecycleScope.launch {
        // 1. 获取可用周期
        val periodsResult = repository.getAvailablePeriods()
        periodsResult.onSuccess { periods ->
            if (periods.isNotEmpty()) {
                // 2. 获取最新周期的排名
                val rankingsResult = repository.getLeaderboard(periods.first())
                rankingsResult.onSuccess { rankings ->
                    // 转换为 Community 格式（复用现有 UI）
                    val communities = rankings.map {
                        Community(it.nickname, it.steps, 0)
                    }
                    adapter.submitList(communities)
                }
            }
        }
    }
}
```

### 3. 更新 ProfileFragment 使用真实商品
```kotlin
private fun loadShopItems() {
    lifecycleScope.launch {
        val result = repository.getRedemptionGoods(vipLevel = 1)
        result.onSuccess { goods ->
            val shopItems = goods.map { good ->
                ShopItem(
                    id = good.id,
                    name = good.name,
                    type = good.category ?: "item",
                    cost = good.redemptionPoints,
                    owned = false
                )
            }
            // 更新 UI
        }
    }
}
```

---

## 💡 提示

1. **先测试，再替换**：先用测试代码确认连接正常，再替换 MockData
2. **保留 Mock 数据**：作为失败时的 fallback
3. **添加加载状态**：使用 ProgressBar 或 Skeleton Screen
4. **错误处理**：显示友好的错误提示，不要让应用崩溃
5. **日志记录**：保留详细的日志，方便调试

---

**准备好了吗？** 

1. 添加测试代码
2. 运行应用
3. 查看 Logcat
4. 告诉我测试结果！

如果看到错误，把完整的 Logcat 日志发给我，我会帮您解决！😊
