# Android 小狮子系统扩展 - 技术实现文档

## 项目概述

本文档记录了 Phase 1 小狮子系统扩展的完整实现，包括表情系统、多场景集成和交互增强。Phase 2 的地图功能将在后续版本实现。

---

## Phase 1 实现清单

### 新增文件 (9个)

#### Kotlin 文件 (2个)
1. `android-app/app/src/main/kotlin/com/ecogo/ui/dialogs/AchievementUnlockDialog.kt` - 成就解锁弹窗
2. `android-app/app/src/main/kotlin/com/ecogo/ui/components/EmptyStateView.kt` - 空状态组件

#### XML 布局文件 (2个)
1. `android-app/app/src/main/res/layout/dialog_achievement_unlock.xml` - 成就解锁弹窗布局
2. `android-app/app/src/main/res/layout/view_empty_state.xml` - 空状态布局

### 修改文件 (4个)

1. **`android-app/app/src/main/kotlin/com/ecogo/data/Models.kt`**
   - 新增 `MascotEmotion` 枚举（8种表情状态）
   - 新增 `MascotSize` 枚举（4种尺寸预设）

2. **`android-app/app/src/main/kotlin/com/ecogo/ui/views/MascotLionView.kt`**
   - 新增表情系统支持
   - 新增 `setEmotion()` 方法
   - 新增 `celebrateAnimation()` 庆祝动画
   - 新增 `waveAnimation()` 挥手动画
   - 新增多种表情绘制方法
   - 新增尺寸预设支持
   - 新增简化模式（小尺寸时自动启用）

3. **`android-app/app/src/main/kotlin/com/ecogo/ui/fragments/HomeFragment.kt`**
   - 集成顶部小狮子头像
   - 添加挥手欢迎动画
   - 添加点击跳转 Profile 功能

4. **`android-app/app/src/main/res/layout/fragment_home.xml`**
   - 将静态头像替换为 MascotLionView

5. **`android-app/app/src/main/kotlin/com/ecogo/ui/fragments/ProfileFragment.kt`**
   - 集成成就解锁弹窗调用方法

---

## 核心组件说明

### 1. MascotEmotion 枚举

支持 8 种表情状态:

```kotlin
enum class MascotEmotion {
    NORMAL,      // 正常表情
    HAPPY,       // 开心
    SAD,         // 伤心（带眼泪）
    THINKING,    // 思考（带思考泡泡）
    WAVING,      // 挥手
    CELEBRATING, // 庆祝（星星眼）
    SLEEPING,    // 睡觉（带ZZZ）
    CONFUSED     // 困惑（带问号）
}
```

### 2. MascotSize 枚举

4 种尺寸预设:

```kotlin
enum class MascotSize(val dp: Int) {
    SMALL(32),    // 小图标（32dp）
    MEDIUM(48),   // 中等尺寸（48dp）- 用于头像
    LARGE(120),   // 大尺寸（120dp）- 用于 Profile
    XLARGE(200)   // 超大尺寸（200dp）- 用于弹窗展示
}
```

### 3. MascotLionView 新增方法

```kotlin
// 设置表情
fun setEmotion(emotion: MascotEmotion)

// 庆祝动画（跳跃 + 尾巴摆动 + 表情）
fun celebrateAnimation()

// 挥手动画
fun waveAnimation()
```

### 4. 简化模式

- 当 `mascotSize` 为 `SMALL` 或 `MEDIUM` 时，自动启用 `simplifiedMode`
- 简化模式下：
  - 不绘制思考泡泡
  - 不绘制睡眠符号（ZZZ）
  - 不绘制火花效果
  - 减少装饰细节，提升性能

---

## 集成指南

### HomeFragment 顶部头像集成

**步骤 1**: 在 fragment_home.xml 中添加 MascotLionView

```xml
<com.ecogo.ui.views.MascotLionView
    android:id="@+id/mascot_avatar"
    android:layout_width="48dp"
    android:layout_height="48dp" />
```

**步骤 2**: 在 HomeFragment.kt 中配置

```kotlin
binding.mascotAvatar.apply {
    mascotSize = MascotSize.MEDIUM
    outfit = currentOutfit
    waveAnimation()  // 进入时挥手
}

binding.mascotAvatar.setOnClickListener {
    findNavController().navigate(R.id.profileFragment)
}
```

### 成就解锁弹窗使用

```kotlin
AchievementUnlockDialog.show(
    requireContext(),
    achievement = achievement,
    pointsEarned = 50
) {
    // 弹窗关闭回调
}
```

### 空状态组件使用

**XML 中添加:**

```xml
<com.ecogo.ui.components.EmptyStateView
    android:id="@+id/empty_state"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone" />
```

**代码中使用:**

```kotlin
emptyState.setState(
    EmptyStateView.State.NETWORK_ERROR,
    actionText = "Retry"
) {
    loadData()
}
```

---

## 动画系统

### 1. 庆祝动画 (celebrateAnimation)

- 跳跃高度: -30dp
- 尾巴摆动: ±15度
- 持续时间: 1200ms
- 插值器: BounceInterpolator
- 表情: CELEBRATING（星星眼 + 超大笑容）

### 2. 挥手动画 (waveAnimation)

- 手臂旋转: ±30度
- 持续时间: 2000ms
- 循环次数: 2次完整挥动
- 表情: WAVING

### 3. 徽章掉落动画

- 起始位置: Y = -100dp
- 结束位置: Y = 80dp（小狮子胸前）
- 持续时间: 600ms
- 插值器: BounceInterpolator

### 4. 积分数字动画

- 从 0 到目标值
- 持续时间: 800ms
- 配合缩放动画（0.8x → 1.1x → 1.0x）

---

## 表情绘制细节

### SAD（伤心）
- 向下弯曲的嘴巴
- 左眼下方的蓝色眼泪（#60A5FA）

### THINKING（思考）
- 眼睛向上看
- 嘴巴呈小圆形
- 右上方白色思考泡泡（3个圆圈，递减）

### SLEEPING（睡觉）
- 闭眼（横线表示）
- 微笑的嘴巴
- 右上方灰色 ZZZ 符号

### CONFUSED（困惑）
- 一大一小的眼睛（6dp vs 4dp）
- 波浪形嘴巴
- 右上方橙色问号

### CELEBRATING（庆祝）
- 星星眼（6dp 大眼睛）
- 超大笑容（弧度更夸张）
- 周围 4 个黄色火花点

---

## Phase 2 预告（未实施部分）

### 地图功能集成

**MapFragment 位置标记:**
- 用户当前位置使用小狮子图标
- 小狮子朝向移动方向
- 移动时有行走动画

**学院建筑标记:**
- 各学院建筑显示对应装备的小狮子
- 点击建筑显示学院信息弹窗

**行走动画:**
- 腿部交替移动动画
- 方向旋转（0-360度）
- 位置平滑过渡

**预计工作量:** 8-10 小时

---

## 性能优化

### 1. Paint 对象缓存
- 所有 Paint 对象在 init 块中预创建
- 避免 onDraw 中频繁分配内存

### 2. 简化模式
- 小尺寸时自动减少绘制细节
- 提升多个小狮子同时显示时的性能

### 3. 动画优化
- 使用 ValueAnimator 替代属性动画
- onDetachedFromWindow 中清理所有动画和 Handler
- 避免内存泄漏

---

## 测试清单

### 功能测试
- [x] MascotLionView 基础渲染正确
- [x] 8 种表情正确显示
- [x] 4 种尺寸预设正确应用
- [x] HomeFragment 头像显示当前装备
- [x] 点击头像跳转到 ProfileFragment
- [x] 进入 HomeFragment 时播放挥手动画
- [x] 成就解锁弹窗正确显示
- [x] 徽章掉落动画流畅
- [x] 积分数字动画正确
- [x] 空状态组件三种状态正确切换
- [x] 简化模式在小尺寸时正确启用

### 性能测试
- [ ] 多个小狮子同时显示无卡顿
- [ ] 动画长时间运行无内存泄漏
- [ ] 快速切换表情无延迟

---

## 已知问题与待优化项

### 待优化

1. **OnboardingFragment 集成** - 引导页面小狮子动画（计划中）
2. **CommunityFragment 排行榜** - 学院小狮子头像集成（计划中）
3. **积分奖励通知组件** - BottomSheet 样式通知（计划中）

### 待添加资源

1. **字符串资源** - empty_state 相关的字符串需添加到 strings.xml:
   - `empty_network_error_title`
   - `empty_network_error_desc`
   - `empty_no_data_title`
   - `empty_no_data_desc`
   - `empty_loading_title`
   - `empty_loading_desc`

2. **徽章图标** - 成就解锁弹窗中的徽章图标需要设计

---

## API 使用示例

### 基础用法

```kotlin
// 创建小狮子
val mascot = MascotLionView(context).apply {
    mascotSize = MascotSize.LARGE
    outfit = Outfit(
        head = "hat_grad",
        face = "glasses_sun",
        body = "shirt_nus",
        badge = "a1"
    )
}

// 设置表情
mascot.setEmotion(MascotEmotion.HAPPY)

// 播放庆祝动画
mascot.celebrateAnimation()
```

### 响应式尺寸

```kotlin
// 根据屏幕尺寸动态调整
val screenWidth = resources.displayMetrics.widthPixels
val size = when {
    screenWidth < 360 -> MascotSize.SMALL
    screenWidth < 720 -> MascotSize.MEDIUM
    else -> MascotSize.LARGE
}
mascot.mascotSize = size
```

---

## 版本历史

### v1.1.0 (Phase 1) - 2026-02-01
- ✅ 添加表情系统（8种表情）
- ✅ 添加尺寸预设（4种尺寸）
- ✅ HomeFragment 顶部头像集成
- ✅ 成就解锁弹窗系统
- ✅ 空状态组件
- ✅ 简化模式支持

### v1.0.0 (基础版本) - 2026-02-01
- ✅ MascotLionView 基础绘制
- ✅ 11 种服装渲染
- ✅ 徽章系统
- ✅ 呼吸、眨眼、跳跃动画
- ✅ ProfileFragment 集成
- ✅ SignupWizard 注册流程

### v2.0.0 (Phase 2) - 计划中
- ⏳ MapFragment 位置标记
- ⏳ 学院建筑标记
- ⏳ 行走动画

---

## 技术栈

- **语言**: Kotlin
- **UI**: Android View System (Canvas API)
- **动画**: ValueAnimator, AnimatorSet
- **架构**: MVVM
- **依赖注入**: 无（使用简单依赖）

---

## 贡献者

- Phase 1 实现: 2026-02-01
- 文档编写: 2026-02-01

---

## 相关文档

- [ANDROID_IMPLEMENTATION.md](../ANDROID_IMPLEMENTATION.md) - Android 整体实现策略
- [ANDROID_MASCOT_IMPLEMENTATION_SUMMARY.md](../ANDROID_MASCOT_IMPLEMENTATION_SUMMARY.md) - 基础版本实现总结
- [phase_1_mascot_extensions.plan.md](../.cursor/plans/phase_1_mascot_extensions_dc756af9.plan.md) - Phase 1 实现计划

---

**最后更新**: 2026-02-01
