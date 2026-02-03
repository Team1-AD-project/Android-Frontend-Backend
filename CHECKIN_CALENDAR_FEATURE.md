# 每日签到日历功能实现

## 概述

将首页的简单打卡按钮升级为完整的日历签到界面,用户可以查看签到历史、连续签到天数以及本月签到统计。

## 功能特性

### 1. 统计卡片
- **连续签到天数**: 显示用户连续签到的天数
- **累计签到次数**: 显示总签到次数
- **本月签到次数**: 显示当月签到次数

### 2. 完整日历视图
- 显示完整的月历
- 支持月份切换(上一月/下一月)
- 已签到日期显示特殊标记(绿色背景 + 勾选图标)
- 当天日期高亮显示
- 未来日期禁用(不可点击)
- 可选择查看历史签到日期详情

### 3. 签到功能
- 大按钮"立即签到"
- 签到成功后显示成功提示(获得积分)
- 今日已签到后按钮变灰并显示"今日已签到"
- 自动刷新日历显示

### 4. 签到规则说明
- 每日签到获得 10 积分
- 连续签到 7 天额外奖励 50 积分
- 连续签到 30 天额外奖励 200 积分
- 每月全勤可获得专属徽章

## 技术实现

### 新增文件

1. **CheckInCalendarFragment.kt**
   - 位置: `android-app/app/src/main/kotlin/com/ecogo/ui/fragments/CheckInCalendarFragment.kt`
   - 功能: 日历签到界面的主要逻辑

2. **fragment_check_in_calendar.xml**
   - 位置: `android-app/app/src/main/res/layout/fragment_check_in_calendar.xml`
   - 功能: 日历签到界面布局

3. **item_calendar_day.xml**
   - 位置: `android-app/app/src/main/res/layout/item_calendar_day.xml`
   - 功能: 日历中单个日期的布局

4. **Drawable资源**
   - `bg_calendar_day.xml`: 普通日期背景
   - `bg_calendar_checked.xml`: 已签到日期背景
   - `bg_calendar_selected.xml`: 选中日期边框
   - `ic_back.xml`: 返回/上一月图标

### 修改文件

1. **HomeFragment.kt**
   - 修改打卡按钮点击事件,从执行签到改为跳转到日历界面
   - 行号: 第 170 行

2. **nav_graph.xml**
   - 添加 `checkInCalendarFragment` 定义
   - 添加从 `homeFragment` 到 `checkInCalendarFragment` 的导航动作

3. **MockData.kt**
   - 添加 `CHECK_IN_HISTORY` 模拟数据(15条签到记录)
   - 用于日历显示历史签到标记

4. **EcoGoRepository.kt**
   - 添加 `getCheckInHistory()` 方法
   - 返回用户的签到历史记录

## 数据模型

使用现有的数据模型:
- `CheckInStatus`: 签到状态信息
- `CheckInResponse`: 签到响应
- `CheckIn`: 签到记录

## 导航流程

```
HomeFragment (首页)
    └─> 点击打卡按钮
        └─> CheckInCalendarFragment (日历签到界面)
            ├─> 查看签到历史
            ├─> 切换月份
            └─> 执行签到
```

## API 接口

### 1. 获取签到状态
```kotlin
suspend fun getCheckInStatus(userId: String): Result<CheckInStatus>
```

### 2. 获取签到历史
```kotlin
suspend fun getCheckInHistory(userId: String): Result<List<CheckIn>>
```

### 3. 执行签到
```kotlin
suspend fun checkIn(userId: String): Result<CheckInResponse>
```

## 界面截图说明

### 顶部统计区
- 渐变背景卡片
- 三列统计信息: 连续签到、累计签到、本月签到

### 中间日历区
- 月份导航: 左右箭头切换月份
- 星期标题: 一二三四五六日
- 日期网格: 7x6 网格布局
- 视觉标识:
  - 普通日期: 白底黑字
  - 今日: 主题色文字加粗
  - 已签到: 主题色背景 + 白字 + 右下角勾选图标
  - 未来日期: 半透明禁用

### 底部操作区
- 大按钮"立即签到"(绿色主题色)
- 签到规则说明卡片(浅绿背景)

### 成功提示
- 顶部滑入的成功消息卡片
- 显示获得的积分
- 自动 3 秒后消失或手动关闭

## 使用流程

1. 用户在首页点击打卡按钮(日历图标)
2. 进入日历签到界面
3. 查看统计信息和历史签到记录
4. 点击"立即签到"按钮
5. 显示成功提示,日历更新显示今日已签到
6. 统计数据自动更新

## 注意事项

1. **日期处理**: 使用 `java.time.LocalDate` API,需要 Android API 26+ 或启用 desugaring
2. **时区**: 所有日期以本地时区为准
3. **Mock数据**: 当前使用模拟数据,实际部署需连接真实API
4. **性能**: 日历重建时会移除所有视图并重新创建,对于大量历史记录需要优化

## 未来改进

- [ ] 添加签到奖励动画效果
- [ ] 支持补签功能(消耗积分)
- [ ] 显示签到奖励进度条(距离下一个里程碑)
- [ ] 添加月度签到成就徽章
- [ ] 支持导出签到报告
- [ ] 添加签到提醒通知
- [ ] 支持自定义签到目标

## 依赖关系

- AndroidX Navigation Component
- Material Design Components
- Kotlin Coroutines
- ViewBinding

## 测试建议

1. 测试不同月份的切换
2. 测试跨月签到连续性
3. 测试已签到状态的持久化
4. 测试网络异常情况的处理
5. 测试不同屏幕尺寸的布局适配
