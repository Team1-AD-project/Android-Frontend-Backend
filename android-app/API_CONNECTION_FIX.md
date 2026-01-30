# 🔧 API 连接修复完成

## ✅ 已修复的问题

### 问题 1: 数据模型不匹配
**问题**: 前端的 `Activity` 和 `Ranking` 数据类与后端不一致

**修复**:
```kotlin
// 旧的 Activity（错误）
data class Activity(
    val id: Int,           // ❌ 后端是 String
    val date: String,      // ❌ 后端没有此字段
    val location: String,  // ❌ 后端没有此字段
    val points: Int        // ❌ 后端是 rewardCredits
)

// 新的 Activity（正确）
data class Activity(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val type: String = "ONLINE",
    val status: String = "DRAFT",
    val rewardCredits: Int = 0,
    val maxParticipants: Int? = null,
    val currentParticipants: Int = 0,
    val startTime: String? = null,
    val endTime: String? = null,
    val createdAt: String? = null
)
```

### 问题 2: API 端点不完整
**问题**: 前端 API 定义缺少很多后端已实现的端点

**修复**: 已添加所有后端支持的端点
- ✅ 活动管理（CRUD + 参加/退出）
- ✅ 排行榜（周期查询 + 排名）
- ✅ 商品管理（列表 + 兑换商品）
- ✅ 订单管理（创建 + 历史 + 兑换订单）
- ✅ 徽章系统（购买 + 佩戴 + 商店）
- ✅ 统计数据（仪表盘统计）

### 问题 3: 后端响应格式不匹配
**问题**: 后端某些接口返回包含 `pagination` 的嵌套结构

**修复**:
```kotlin
// 商品列表响应
data class GoodsResponse(
    val data: List<GoodsDto>,
    val pagination: PaginationDto
)

// 订单历史响应
data class OrderHistoryResponse(
    val data: List<OrderSummaryDto>,
    val pagination: PaginationDto
)
```

---

## 📋 完整的 API 端点列表

### 活动管理 (`/api/v1/activities`)
| 方法 | 端点 | 功能 | 状态 |
|------|------|------|------|
| GET | `/api/v1/activities` | 获取所有活动 | ✅ |
| GET | `/api/v1/activities/{id}` | 获取活动详情 | ✅ |
| POST | `/api/v1/activities` | 创建活动 | ✅ |
| PUT | `/api/v1/activities/{id}` | 更新活动 | ✅ |
| DELETE | `/api/v1/activities/{id}` | 删除活动 | ✅ |
| GET | `/api/v1/activities/status/{status}` | 按状态查询 | ✅ |
| POST | `/api/v1/activities/{id}/join` | 参加活动 | ✅ |
| POST | `/api/v1/activities/{id}/leave` | 退出活动 | ✅ |

### 排行榜 (`/api/v1/leaderboards`)
| 方法 | 端点 | 功能 | 状态 |
|------|------|------|------|
| GET | `/api/v1/leaderboards/periods` | 获取可用周期 | ✅ |
| GET | `/api/v1/leaderboards/rankings` | 获取排名（带period参数）| ✅ |

### 商品管理 (`/api/v1/goods`)
| 方法 | 端点 | 功能 | 状态 |
|------|------|------|------|
| GET | `/api/v1/goods` | 获取商品列表（带分页、筛选）| ✅ |
| GET | `/api/v1/goods/{id}` | 获取商品详情 | ✅ |
| GET | `/api/v1/goods/mobile/redemption` | 获取可兑换商品 | ✅ |

### 订单管理 (`/api/v1/orders`)
| 方法 | 端点 | 功能 | 状态 |
|------|------|------|------|
| POST | `/api/v1/orders` | 创建订单 | ✅ |
| POST | `/api/v1/orders/redemption` | 创建兑换订单 | ✅ |
| GET | `/api/v1/orders/mobile/user/{userId}` | 获取用户订单历史 | ✅ |
| PUT | `/api/v1/orders/{id}/status` | 更新订单状态 | ✅ |

### 徽章系统 (`/api/v1/mobile/badges`)
| 方法 | 端点 | 功能 | 状态 |
|------|------|------|------|
| POST | `/api/v1/mobile/badges/{badge_id}/purchase` | 购买徽章 | ✅ |
| PUT | `/api/v1/mobile/badges/{badge_id}/display` | 佩戴/卸下徽章 | ✅ |
| GET | `/api/v1/mobile/badges/shop` | 获取商店列表 | ✅ |
| GET | `/api/v1/mobile/badges/user/{user_id}` | 获取我的徽章 | ✅ |

### 统计数据 (`/api/v1/statistics`)
| 方法 | 端点 | 功能 | 状态 |
|------|------|------|------|
| GET | `/api/v1/statistics/dashboard` | 获取仪表盘统计 | ✅ |

---

## 🔗 如何在 Fragment 中使用

### 示例 1: 加载活动列表（ActivitiesFragment）

```kotlin
import androidx.lifecycle.lifecycleScope
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch
import android.util.Log

class ActivitiesFragment : Fragment() {
    private val repository = EcoGoRepository()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadActivities()
    }
    
    private fun loadActivities() {
        lifecycleScope.launch {
            try {
                val result = repository.getAllActivities()
                result.onSuccess { activities ->
                    Log.d("API", "✅ 成功加载 ${activities.size} 个活动")
                    // 更新 UI
                    adapter.submitList(activities)
                }.onFailure { error ->
                    Log.e("API", "❌ 加载失败: ${error.message}")
                    // 显示错误或使用 Mock 数据
                    Toast.makeText(context, "加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "❌ 异常: ${e.message}")
            }
        }
    }
}
```

### 示例 2: 加载排行榜（CommunityFragment）

```kotlin
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {
    private val repository = EcoGoRepository()
    
    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                // 1. 获取可用周期
                val periodsResult = repository.getAvailablePeriods()
                periodsResult.onSuccess { periods ->
                    if (periods.isNotEmpty()) {
                        val currentPeriod = periods.first() // 使用最新周期
                        
                        // 2. 获取该周期的排名
                        val rankingsResult = repository.getLeaderboard(currentPeriod)
                        rankingsResult.onSuccess { rankings ->
                            Log.d("API", "✅ 加载 ${rankings.size} 条排名")
                            // 转换为 Community 数据（用于现有 UI）
                            val communities = rankings.map { ranking ->
                                Community(
                                    name = ranking.nickname,
                                    points = ranking.steps,
                                    change = 0 // 后端暂无变化数据
                                )
                            }
                            adapter.submitList(communities)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API", "❌ 加载失败: ${e.message}")
            }
        }
    }
}
```

### 示例 3: 加载商店商品（ProfileFragment）

```kotlin
private fun loadShopItems() {
    lifecycleScope.launch {
        try {
            val result = repository.getRedemptionGoods()
            result.onSuccess { goods ->
                Log.d("API", "✅ 加载 ${goods.size} 个商品")
                // 转换为 ShopItem（用于现有 UI）
                val shopItems = goods.map { good ->
                    ShopItem(
                        id = good.id,
                        name = good.name,
                        type = good.category ?: "item",
                        cost = good.redemptionPoints,
                        owned = false // 需要另外查询用户订单
                    )
                }
                // 更新 UI
            }
        } catch (e: Exception) {
            Log.e("API", "❌ 加载失败: ${e.message}")
        }
    }
}
```

---

## 🐛 调试步骤

### 步骤 1: Sync Gradle
```
File > Sync Project with Gradle Files
等待同步完成
```

### 步骤 2: 启动后端
```
方法 A: IntelliJ IDEA
1. 打开项目: C:\Users\csls\Desktop\ad-ui
2. 运行 EcoGoApplication
3. 等待看到 "Started EcoGoApplication"

方法 B: 命令行
cd C:\Users\csls\Desktop\ad-ui
mvn spring-boot:run
```

### 步骤 3: 验证后端运行
浏览器访问：`http://localhost:8090/actuator/health`

应该返回：
```json
{
  "status": "UP"
}
```

### 步骤 4: 测试 API 连接
在任意 Fragment 中添加测试代码：

```kotlin
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.ecogo.repository.EcoGoRepository
import kotlinx.coroutines.launch

private fun testConnection() {
    lifecycleScope.launch {
        try {
            Log.d("API_TEST", "🔄 测试连接...")
            
            val result = repository.getAllActivities()
            result.onSuccess { activities ->
                Log.d("API_TEST", "✅ 成功！获取到 ${activities.size} 个活动")
                activities.forEach {
                    Log.d("API_TEST", "  - ${it.title}")
                }
            }.onFailure { error ->
                Log.e("API_TEST", "❌ 失败: ${error.message}")
                Log.e("API_TEST", "错误详情: ", error)
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "❌ 异常: ${e.message}")
        }
    }
}
```

### 步骤 5: 查看 Logcat
在 Android Studio 中：
```
Logcat > 过滤: API_TEST 或 OkHttp
```

---

## 🔍 故障排除

### 错误 1: `Failed to connect to /10.0.2.2:8090`
**原因**: 后端未启动或端口错误

**解决方案**:
```
1. 确认后端运行: http://localhost:8090/actuator/health
2. 确认端口号: application.yaml 中的 server.port
3. 模拟器使用 10.0.2.2 访问本机
4. 真实设备使用电脑IP（如 192.168.1.100）
```

### 错误 2: `HTTP 404 Not Found`
**原因**: API 路径错误

**解决方案**:
```
检查 ApiService.kt 中的路径是否与后端 Controller 匹配
后端路径格式: /api/v1/{endpoint}
```

### 错误 3: `JSON parse error`
**原因**: 后端返回的 JSON 结构与前端 DTO 不匹配

**解决方案**:
```
1. 查看 OkHttp 日志（完整的 JSON 响应）
2. 对照后端 Controller 返回的数据结构
3. 更新前端 DTO 类
```

### 错误 4: 后端返回 `code: 200` 但 `data: null`
**原因**: 数据库中没有数据

**解决方案**:
```
1. 使用 Postman 测试后端端点
2. 检查 MongoDB 是否有数据
3. 使用后端的测试数据接口插入数据
```

---

## 📊 后端数据结构对照

### Activity（活动）
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "507f1f77bcf86cd799439011",
      "title": "校园骑行活动",
      "description": "周末骑行活动，减少碳排放",
      "type": "OFFLINE",
      "status": "PUBLISHED",
      "rewardCredits": 100,
      "maxParticipants": 50,
      "currentParticipants": 12,
      "startTime": "2026-02-01T10:00:00",
      "endTime": "2026-02-01T16:00:00",
      "createdAt": "2026-01-29T12:00:00"
    }
  ]
}
```

### Ranking（排行榜）
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "507f1f77bcf86cd799439012",
      "period": "Week 4, 2026",
      "rank": 1,
      "userId": "user123",
      "nickname": "张三",
      "steps": 15000,
      "isVip": true
    }
  ]
}
```

### Goods（商品）
```json
{
  "code": 200,
  "message": "获取商品列表成功",
  "data": [
    {
      "id": "507f1f77bcf86cd799439013",
      "name": "环保水杯",
      "description": "可重复使用的不锈钢水杯",
      "price": 59.99,
      "stock": 100,
      "category": "日常用品",
      "imageUrl": "/images/water-cup.jpg",
      "isForRedemption": true,
      "redemptionPoints": 500,
      "vipLevelRequired": 0
    }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 5,
    "totalPages": 1
  }
}
```

---

## 🎯 下一步操作

### 选项 A: 测试 API 连接
```
1. Sync Gradle
2. 启动后端（IntelliJ IDEA 或 Maven）
3. 运行 Android 应用
4. 查看 Logcat 中的 API_TEST 日志
```

### 选项 B: 更新现有 Fragment 使用真实 API
```
1. 选择一个 Fragment（如 ActivitiesFragment）
2. 替换 MockData 为 repository 调用
3. 处理加载状态和错误
4. 测试功能
```

### 选项 C: 排查连接问题
```
如果连接失败，告诉我具体的错误信息：
- Logcat 中的完整错误日志
- OkHttp 请求日志
- 后端是否正常运行
```

---

## 📚 相关文件

- `ApiService.kt` - ✅ 已更新所有端点
- `Models.kt` - ✅ 已更新 Activity 和 Ranking 数据类
- `EcoGoRepository.kt` - ⚠️ 需要添加更多方法（排行榜、商品、订单）
- `ApiConfig.kt` - ✅ 已配置 BASE_URL

---

**🎉 API 接口已更新为真实的后端端点！**

现在您可以：
1. Sync Gradle
2. 启动后端
3. 运行 Android 应用测试连接

如果遇到任何错误，请告诉我具体的错误日志，我会帮您解决！
